/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#include <errno.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>

#ifdef __solaris__
#include <fcntl.h>
#endif
#ifdef __linux__
#include <unistd.h>
//#include <sys/sysctl.h>
#include <sys/utsname.h>
#include <netinet/ip.h>

#define IPV6_MULTICAST_IF 17
#ifndef SO_BSDCOMPAT
#define SO_BSDCOMPAT  14
#endif
/**
 * IP_MULTICAST_ALL has been supported since kernel version 2.6.31
 * but we may be building on a machine that is older than that.
 */
#ifndef IP_MULTICAST_ALL
#define IP_MULTICAST_ALL      49
#endif
#endif  //  __linux__

#ifndef IPTOS_TOS_MASK
#define IPTOS_TOS_MASK 0x1e
#endif
#ifndef IPTOS_PREC_MASK
#define IPTOS_PREC_MASK 0xe0
#endif

#include "jvm.h"
#include "jni_util.h"
#include "net_util.h"

#include "java_net_SocketOptions.h"
#include "java_net_PlainDatagramSocketImpl.h"
#include "JNIHelp.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }
/************************************************************************
 * PlainDatagramSocketImpl
 */

static jfieldID IO_fd_fdID;

static jfieldID pdsi_fdID;
static jfieldID pdsi_timeoutID;
static jfieldID pdsi_trafficClassID;
static jfieldID pdsi_localPortID;
static jfieldID pdsi_connected;
static jfieldID pdsi_connectedAddress;
static jfieldID pdsi_connectedPort;

extern void setDefaultScopeID(JNIEnv *env, struct sockaddr *him);
extern int getDefaultScopeID(JNIEnv *env);

/*
 * Returns a java.lang.Integer based on 'i'
 */
static jobject createInteger(JNIEnv *env, int i) {
    static jclass i_class;
    static jmethodID i_ctrID;

    if (i_class == NULL) {
        jclass c = (*env)->FindClass(env, "java/lang/Integer");
        CHECK_NULL_RETURN(c, NULL);
        i_ctrID = (*env)->GetMethodID(env, c, "<init>", "(I)V");
        CHECK_NULL_RETURN(i_ctrID, NULL);
        i_class = (*env)->NewGlobalRef(env, c);
        CHECK_NULL_RETURN(i_class, NULL);
    }

    return ( (*env)->NewObject(env, i_class, i_ctrID, i) );
}

/*
 * Returns a java.lang.Boolean based on 'b'
 */
static jobject createBoolean(JNIEnv *env, int b) {
    static jclass b_class;
    static jmethodID b_ctrID;

    if (b_class == NULL) {
        jclass c = (*env)->FindClass(env, "java/lang/Boolean");
        CHECK_NULL_RETURN(c, NULL);
        b_ctrID = (*env)->GetMethodID(env, c, "<init>", "(Z)V");
        CHECK_NULL_RETURN(b_ctrID, NULL);
        b_class = (*env)->NewGlobalRef(env, c);
        CHECK_NULL_RETURN(b_class, NULL);
    }

    return( (*env)->NewObject(env, b_class, b_ctrID, (jboolean)(b!=0)) );
}


/*
 * Returns the fd for a PlainDatagramSocketImpl or -1
 * if closed.
 */
static int getFD(JNIEnv *env, jobject this) {
    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    if (fdObj == NULL) {
        return -1;
    }
    return (*env)->GetIntField(env, fdObj, IO_fd_fdID);
}


/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL
PlainDatagramSocketImpl_init(JNIEnv *env, jclass cls) {

#ifdef __linux__
    struct utsname sysinfo;
#endif
    pdsi_fdID = (*env)->GetFieldID(env, cls, "fd",
                                   "Ljava/io/FileDescriptor;");
    CHECK_NULL(pdsi_fdID);
    pdsi_timeoutID = (*env)->GetFieldID(env, cls, "timeout", "I");
    CHECK_NULL(pdsi_timeoutID);
    pdsi_trafficClassID = (*env)->GetFieldID(env, cls, "trafficClass", "I");
    CHECK_NULL(pdsi_trafficClassID);
    pdsi_localPortID = (*env)->GetFieldID(env, cls, "localPort", "I");
    CHECK_NULL(pdsi_localPortID);
    pdsi_connected = (*env)->GetFieldID(env, cls, "connected", "Z");
    CHECK_NULL(pdsi_connected);
    pdsi_connectedAddress = (*env)->GetFieldID(env, cls, "connectedAddress",
                                               "Ljava/net/InetAddress;");
    CHECK_NULL(pdsi_connectedAddress);
    pdsi_connectedPort = (*env)->GetFieldID(env, cls, "connectedPort", "I");
    CHECK_NULL(pdsi_connectedPort);

    IO_fd_fdID = NET_GetFileDescriptorID(env);
    CHECK_NULL(IO_fd_fdID);
}

/*
 * Set outgoing multicast interface designated by a NetworkInterface index.
 * Throw exception if failed.
 *
 * Android changed: return 0 on success, negative on failure.
 * Android changed: Interface index (not NetworkInterface) as the parameter
 */
static int mcast_set_if_by_if_v4(JNIEnv *env, jobject this, int fd, jint ifindex) {
    struct ip_mreqn req;
    memset(&req, 0, sizeof(req));
    req.imr_ifindex = ifindex;

    if (JVM_SetSockOpt(fd, IPPROTO_IP, IP_MULTICAST_IF,
                       (const char*)&req, sizeof(req)) < 0) {
        NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException",
                       "Error setting socket option");
        return -1;
    }

    return 0;
}

/*
 * Set outgoing multicast interface designated by a NetworkInterface.
 * Throw exception if failed.
 * Android changed: Interface index (not NetworkInterface) as the parameter
 */
static void mcast_set_if_by_if_v6(JNIEnv *env, jobject this, int fd, jint ifindex) {
    if (JVM_SetSockOpt(fd, IPPROTO_IPV6, IPV6_MULTICAST_IF,
                       (const char*)&ifindex, sizeof(ifindex)) < 0) {
        if (errno == EINVAL && ifindex > 0) {
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                "IPV6_MULTICAST_IF failed (interface has IPv4 "
                "address only?)");
        } else {
            NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException",
                           "Error setting socket option");
        }
        return;
    }
}

/*
 * Set outgoing multicast interface designated by an InetAddress.
 * Throw exception if failed.
 *
 * Android-changed : Return type, return 0 on success, negative on failure.
 */
static int mcast_set_if_by_addr_v4(JNIEnv *env, jobject this, int fd, jobject value) {
    struct in_addr in;

    in.s_addr = htonl( getInetAddress_addr(env, value) );

    if (JVM_SetSockOpt(fd, IPPROTO_IP, IP_MULTICAST_IF,
                       (const char*)&in, sizeof(in)) < 0) {
        NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException",
                         "Error setting socket option");
        return -1;
    }

    return 0;
}

/*
 * Set outgoing multicast interface designated by an InetAddress.
 * Throw exception if failed.
 */
static void mcast_set_if_by_addr_v6(JNIEnv *env, jobject this, int fd, jobject value) {
    static jclass ni_class;
    static jmethodID ni_getByInetAddress;
    static jmethodID ni_getIndex;
    if (ni_class == NULL) {
        jclass c = (*env)->FindClass(env, "java/net/NetworkInterface");
        CHECK_NULL(c);
        ni_class = (*env)->NewGlobalRef(env, c);
        CHECK_NULL(ni_class);
        ni_getByInetAddress = (*env)->GetStaticMethodID(
            env, ni_class, "getByInetAddress", "(Ljava/net/InetAddress;)Ljava/net/NetworkInterface;");
        CHECK_NULL(ni_getByInetAddress);
        ni_getIndex = (*env)->GetMethodID(
            env, ni_class, "getIndex", "()I");
        CHECK_NULL(ni_getIndex);
    }

    /*
     * Get the NetworkInterface by inetAddress
     */
    jobject ni_value = (*env)->CallStaticObjectMethod(
        env, ni_class, ni_getByInetAddress, value);
    if (ni_value == NULL) {
        if (!(*env)->ExceptionOccurred(env)) {
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                 "bad argument for IP_MULTICAST_IF"
                 ": address not bound to any interface");
        }
        return;
    }

    /*
     * Get the NetworkInterface index
     */
    jint ifindex = (*env)->CallIntMethod(env, ni_value, ni_getIndex);
    if ((*env)->ExceptionOccurred(env)) {
        return;
    }

    mcast_set_if_by_if_v6(env, this, fd, ifindex);
}

/*
 * Sets the multicast interface.
 *
 * SocketOptions.IP_MULTICAST_IF :-
 *      value is a InetAddress
 *      IPv4:   set outgoing multicast interface using
 *              IPPROTO_IP/IP_MULTICAST_IF
 *      IPv6:   Get the index of the interface to which the
 *              InetAddress is bound
 *              Set outgoing multicast interface using
 *              IPPROTO_IPV6/IPV6_MULTICAST_IF
 *
 * SockOptions.IF_MULTICAST_IF2 :-
 *      value is a NetworkInterface
 *      IPv4:   Obtain IP address bound to network interface
 *              (NetworkInterface.addres[0])
 *              set outgoing multicast interface using
 *              IPPROTO_IP/IP_MULTICAST_IF
 *      IPv6:   Obtain NetworkInterface.index
 *              Set outgoing multicast interface using
 *              IPPROTO_IPV6/IPV6_MULTICAST_IF
 *
 */
static void setMulticastInterface(JNIEnv *env, jobject this, int fd,
                                  jint opt, jobject value)
{
    if (opt == java_net_SocketOptions_IP_MULTICAST_IF) {
        /*
         * value is an InetAddress.
         */
        // Android-changed: Return early if mcast_set_if_by_addr_v4 threw.
        // We don't want to call into the IPV6 code with a pending exception.
        if (mcast_set_if_by_addr_v4(env, this, fd, value)) {
            return;
        }
        if (ipv6_available()) {
            mcast_set_if_by_addr_v6(env, this, fd, value);
        }
    }

    if (opt == java_net_SocketOptions_IP_MULTICAST_IF2) {
      /*
         * value is a Integer (Android-changed, openJdk uses NetworkInterface)
         */
        static jfieldID integer_valueID;
        if (integer_valueID == NULL) {
            jclass c = (*env)->FindClass(env, "java/lang/Integer");
            CHECK_NULL(c);
            integer_valueID = (*env)->GetFieldID(env, c, "value", "I");
            CHECK_NULL(integer_valueID);
        }
        int index = (*env)->GetIntField(env, value, integer_valueID);

        // Android-changed: Return early if mcast_set_if_by_addr_v4 threw.
        // We don't want to call into the IPV6 code with a pending exception.
        if (mcast_set_if_by_if_v4(env, this, fd, index)) {
            return;
        }
        if (ipv6_available()) {
            mcast_set_if_by_if_v6(env, this, fd, index);
        }
    }
}

/*
 * Enable/disable local loopback of multicast datagrams.
 */
static void mcast_set_loop_v4(JNIEnv *env, jobject this, int fd, jobject value) {
    jclass cls;
    jfieldID fid;
    jboolean on;
    char loopback;

    cls = (*env)->FindClass(env, "java/lang/Boolean");
    CHECK_NULL(cls);
    fid =  (*env)->GetFieldID(env, cls, "value", "Z");
    CHECK_NULL(fid);

    on = (*env)->GetBooleanField(env, value, fid);
    loopback = (!on ? 1 : 0);

    if (NET_SetSockOpt(fd, IPPROTO_IP, IP_MULTICAST_LOOP, (const void *)&loopback, sizeof(char)) < 0) {
        NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException", "Error setting socket option");
        return;
    }
}

/*
 * Enable/disable local loopback of multicast datagrams.
 */
#ifdef AF_INET6
static void mcast_set_loop_v6(JNIEnv *env, jobject this, int fd, jobject value) {
    jclass cls;
    jfieldID fid;
    jboolean on;
    int loopback;

    cls = (*env)->FindClass(env, "java/lang/Boolean");
    CHECK_NULL(cls);
    fid =  (*env)->GetFieldID(env, cls, "value", "Z");
    CHECK_NULL(fid);

    on = (*env)->GetBooleanField(env, value, fid);
    loopback = (!on ? 1 : 0);

    if (NET_SetSockOpt(fd, IPPROTO_IPV6, IPV6_MULTICAST_LOOP, (const void *)&loopback, sizeof(int)) < 0) {
        NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException", "Error setting socket option");
        return;
    }

}
#endif  /* AF_INET6 */

/*
 * Sets the multicast loopback mode.
 */
static void setMulticastLoopbackMode(JNIEnv *env, jobject this, int fd,
                                  jint opt, jobject value) {
#ifdef AF_INET6
#ifdef __linux__
    mcast_set_loop_v4(env, this, fd, value);
    if (ipv6_available()) {
        mcast_set_loop_v6(env, this, fd, value);
    }
#else  /* __linux__ not defined */
    if (ipv6_available()) {
        mcast_set_loop_v6(env, this, fd, value);
    } else {
        mcast_set_loop_v4(env, this, fd, value);
    }
#endif  /* __linux__ */
#else
    mcast_set_loop_v4(env, this, fd, value);
#endif  /* AF_INET6 */
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    socketSetOption0
 * Signature: (ILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL
PlainDatagramSocketImpl_socketSetOption0(JNIEnv *env,
                                                      jobject this,
                                                      jint opt,
                                                      jobject value) {
    int fd;
    int level, optname, optlen;
    union {
        int i;
        char c;
    } optval;

    /*
     * Check that socket hasn't been closed
     */
    fd = getFD(env, this);
    if (fd < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Socket closed");
        return;
    }

    /*
     * Check argument has been provided
     */
    if (IS_NULL(value)) {
        JNU_ThrowNullPointerException(env, "value argument");
        return;
    }

    /*
     * Setting the multicast interface handled seperately
     */
    if (opt == java_net_SocketOptions_IP_MULTICAST_IF ||
        opt == java_net_SocketOptions_IP_MULTICAST_IF2) {

        setMulticastInterface(env, this, fd, opt, value);
        return;
    }

    /*
     * Setting the multicast loopback mode handled separately
     */
    if (opt == java_net_SocketOptions_IP_MULTICAST_LOOP) {
        setMulticastLoopbackMode(env, this, fd, opt, value);
        return;
    }

    /*
     * Map the Java level socket option to the platform specific
     * level and option name.
     */
    if (NET_MapSocketOption(opt, &level, &optname)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Invalid option");
        return;
    }

    switch (opt) {
        case java_net_SocketOptions_SO_SNDBUF :
        case java_net_SocketOptions_SO_RCVBUF :
        case java_net_SocketOptions_IP_TOS :
            {
                jclass cls;
                jfieldID fid;

                cls = (*env)->FindClass(env, "java/lang/Integer");
                CHECK_NULL(cls);
                fid =  (*env)->GetFieldID(env, cls, "value", "I");
                CHECK_NULL(fid);

                optval.i = (*env)->GetIntField(env, value, fid);
                optlen = sizeof(optval.i);
                break;
            }

        case java_net_SocketOptions_SO_REUSEADDR:
        case java_net_SocketOptions_SO_BROADCAST:
            {
                jclass cls;
                jfieldID fid;
                jboolean on;

                cls = (*env)->FindClass(env, "java/lang/Boolean");
                CHECK_NULL(cls);
                fid =  (*env)->GetFieldID(env, cls, "value", "Z");
                CHECK_NULL(fid);

                on = (*env)->GetBooleanField(env, value, fid);

                /* SO_REUSEADDR or SO_BROADCAST */
                optval.i = (on ? 1 : 0);
                optlen = sizeof(optval.i);

                break;
            }

        default :
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                "Socket option not supported by PlainDatagramSocketImp");
            return;

    }

    if (NET_SetSockOpt(fd, level, optname, (const void *)&optval, optlen) < 0) {
        NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException", "Error setting socket option");
        return;
    }
}


/*
 * Return the multicast interface:
 *
 * SocketOptions.IP_MULTICAST_IF
 *      IPv4:   Query IPPROTO_IP/IP_MULTICAST_IF
 *              Create InetAddress
 *              IP_MULTICAST_IF returns struct ip_mreqn on 2.2
 *              kernel but struct in_addr on 2.4 kernel
 *      IPv6:   Query IPPROTO_IPV6 / IPV6_MULTICAST_IF
 *              If index == 0 return InetAddress representing
 *              anyLocalAddress.
 *              If index > 0 query NetworkInterface by index
 *              and returns addrs[0]
 *
 * SocketOptions.IP_MULTICAST_IF2
 *      IPv4:   Query IPPROTO_IP/IP_MULTICAST_IF
 *              Query NetworkInterface by IP address and
 *              return the NetworkInterface that the address
 *              is bound too.
 *      IPv6:   Query IPPROTO_IPV6 / IPV6_MULTICAST_IF
 *              (except Linux .2 kernel)
 *              Query NetworkInterface by index and
 *              return NetworkInterface.
 */
jobject getMulticastInterface(JNIEnv *env, jobject this, int fd, jint opt) {
    if ((opt == java_net_SocketOptions_IP_MULTICAST_IF2) ||
        (opt == java_net_SocketOptions_IP_MULTICAST_IF)) {
        int index;
        int len = sizeof(index);

        if (JVM_GetSockOpt(fd, IPPROTO_IPV6, IPV6_MULTICAST_IF,
                           (char*)&index, &len) < 0) {
            NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException",
                           "Error getting socket option");
            return NULL;
        }

        jobject ifindex = createInteger(env, index);
        CHECK_NULL_RETURN(ifindex, NULL);
        return ifindex;
    }
    return NULL;
}



/*
 * Returns relevant info as a jint.
 *
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    socketGetOption
 * Signature: (I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL
PlainDatagramSocketImpl_socketGetOption(JNIEnv *env, jobject this,
                                                      jint opt) {
    int fd;
    int level, optname, optlen;
    union {
        int i;
        char c;
    } optval;

    fd = getFD(env, this);
    if (fd < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "socket closed");
        return NULL;
    }

    /*
     * Handle IP_MULTICAST_IF separately
     */
    if (opt == java_net_SocketOptions_IP_MULTICAST_IF ||
        opt == java_net_SocketOptions_IP_MULTICAST_IF2) {
        return getMulticastInterface(env, this, fd, opt);

    }

    /*
     * SO_BINDADDR implemented using getsockname
     */
    if (opt == java_net_SocketOptions_SO_BINDADDR) {
        /* find out local IP address */
        SOCKADDR him;
        socklen_t len = 0;
        int port;
        jobject iaObj;

        len = SOCKADDR_LEN;

        if (getsockname(fd, (struct sockaddr *)&him, &len) == -1) {
            NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException",
                           "Error getting socket name");
            return NULL;
        }
        iaObj = NET_SockaddrToInetAddress(env, (struct sockaddr *)&him, &port);

        return iaObj;
    }

    /*
     * Map the Java level socket option to the platform specific
     * level and option name.
     */
    if (NET_MapSocketOption(opt, &level, &optname)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Invalid option");
        return NULL;
    }

    if (opt == java_net_SocketOptions_IP_MULTICAST_LOOP &&
        level == IPPROTO_IP) {
        optlen = sizeof(optval.c);
    } else {
        optlen = sizeof(optval.i);
    }

    if (NET_GetSockOpt(fd, level, optname, (void *)&optval, &optlen) < 0) {
        NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException",
                         "Error getting socket option");
        return NULL;
    }

    switch (opt) {
        case java_net_SocketOptions_IP_MULTICAST_LOOP:
            /* getLoopbackMode() returns true if IP_MULTICAST_LOOP disabled */
            if (level == IPPROTO_IP) {
                return createBoolean(env, (int)!optval.c);
            } else {
                return createBoolean(env, !optval.i);
            }

        case java_net_SocketOptions_SO_BROADCAST:
        case java_net_SocketOptions_SO_REUSEADDR:
            return createBoolean(env, optval.i);

        case java_net_SocketOptions_SO_SNDBUF:
        case java_net_SocketOptions_SO_RCVBUF:
        case java_net_SocketOptions_IP_TOS:
            return createInteger(env, optval.i);

    }

    /* should never reach here */
    return NULL;
}

static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(PlainDatagramSocketImpl, socketGetOption, "(I)Ljava/lang/Object;"),
  NATIVE_METHOD(PlainDatagramSocketImpl, socketSetOption0, "(ILjava/lang/Object;)V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, init, "()V"),
};

void register_java_net_PlainDatagramSocketImpl(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/net/PlainDatagramSocketImpl", gMethods, NELEM(gMethods));
}
