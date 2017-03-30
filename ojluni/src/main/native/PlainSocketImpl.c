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
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#if defined(__linux__) && !defined(USE_SELECT)
#include <sys/poll.h>
#endif
#include <netinet/tcp.h>        /* Defines TCP_NODELAY, needed for 2.6 */
#include <netinet/in.h>
#ifdef __linux__
#include <netinet/ip.h>
#endif
#include <netdb.h>
#include <stdlib.h>

#ifdef __solaris__
#include <fcntl.h>
#endif
#ifdef __linux__
#include <unistd.h>
//#include <sys/sysctl.h>
#endif

#include "jvm.h"
#include "jni_util.h"
#include "net_util.h"

#include "java_net_SocketOptions.h"
#include "java_net_PlainSocketImpl.h"
#include "JNIHelp.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

/************************************************************************
 * PlainSocketImpl
 */

static jfieldID IO_fd_fdID;

jfieldID psi_fdID;
jfieldID psi_addressID;
jfieldID psi_ipaddressID;
jfieldID psi_portID;
jfieldID psi_localportID;
jfieldID psi_timeoutID;
jfieldID psi_trafficClassID;
jfieldID psi_serverSocketID;
jfieldID psi_fdLockID;
jfieldID psi_closePendingID;

extern void setDefaultScopeID(JNIEnv *env, struct sockaddr *him);


#define SET_NONBLOCKING(fd) {           \
        int flags = fcntl(fd, F_GETFL); \
        flags |= O_NONBLOCK;            \
        fcntl(fd, F_SETFL, flags);      \
}

#define SET_BLOCKING(fd) {              \
        int flags = fcntl(fd, F_GETFL); \
        flags &= ~O_NONBLOCK;           \
        fcntl(fd, F_SETFL, flags);      \
}

/*
 * Return the file descriptor given a PlainSocketImpl
 */
static int getFD(JNIEnv *env, jobject this) {
    jobject fdObj = (*env)->GetObjectField(env, this, psi_fdID);
    CHECK_NULL_RETURN(fdObj, -1);
    return (*env)->GetIntField(env, fdObj, IO_fd_fdID);
}

static void PlainSocketImpl_initProto(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "java/net/PlainSocketImpl");
    psi_fdID = (*env)->GetFieldID(env, cls , "fd",
                                  "Ljava/io/FileDescriptor;");
    CHECK_NULL(psi_fdID);
    psi_addressID = (*env)->GetFieldID(env, cls, "address",
                                          "Ljava/net/InetAddress;");
    CHECK_NULL(psi_addressID);
    psi_portID = (*env)->GetFieldID(env, cls, "port", "I");
    CHECK_NULL(psi_portID);
    psi_localportID = (*env)->GetFieldID(env, cls, "localport", "I");
    CHECK_NULL(psi_localportID);
    psi_timeoutID = (*env)->GetFieldID(env, cls, "timeout", "I");
    CHECK_NULL(psi_timeoutID);
    psi_trafficClassID = (*env)->GetFieldID(env, cls, "trafficClass", "I");
    CHECK_NULL(psi_trafficClassID);
    psi_serverSocketID = (*env)->GetFieldID(env, cls, "serverSocket",
                        "Ljava/net/ServerSocket;");
    CHECK_NULL(psi_serverSocketID);
    psi_fdLockID = (*env)->GetFieldID(env, cls, "fdLock",
                                      "Ljava/lang/Object;");
    CHECK_NULL(psi_fdLockID);
    psi_closePendingID = (*env)->GetFieldID(env, cls, "closePending", "Z");
    CHECK_NULL(psi_closePendingID);
    IO_fd_fdID = NET_GetFileDescriptorID(env);
    CHECK_NULL(IO_fd_fdID);
}

/* a global reference to the java.net.SocketException class. In
 * socketCreate, we ensure that this is initialized. This is to
 * prevent the problem where socketCreate runs out of file
 * descriptors, and is then unable to load the exception class.
 */
static jclass socketExceptionCls;

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketSetOption0
 * Signature: (IZLjava/lang/Object;)V
 */
JNIEXPORT void JNICALL
PlainSocketImpl_socketSetOption0(JNIEnv *env, jobject this,
                                              jint cmd, jboolean on,
                                              jobject value) {
    int fd;
    int level, optname, optlen;
    union {
        int i;
        struct linger ling;
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
     * SO_TIMEOUT is a NOOP on Solaris/Linux
     */
    if (cmd == java_net_SocketOptions_SO_TIMEOUT) {
        return;
    }

    /*
     * Map the Java level socket option to the platform specific
     * level and option name.
     */
    if (NET_MapSocketOption(cmd, &level, &optname)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Invalid option");
        return;
    }

    switch (cmd) {
        case java_net_SocketOptions_SO_SNDBUF :
        case java_net_SocketOptions_SO_RCVBUF :
        case java_net_SocketOptions_SO_LINGER :
        case java_net_SocketOptions_IP_TOS :
            {
                jclass cls;
                jfieldID fid;

                cls = (*env)->FindClass(env, "java/lang/Integer");
                CHECK_NULL(cls);
                fid = (*env)->GetFieldID(env, cls, "value", "I");
                CHECK_NULL(fid);

                if (cmd == java_net_SocketOptions_SO_LINGER) {
                    if (on) {
                        optval.ling.l_onoff = 1;
                        optval.ling.l_linger = (*env)->GetIntField(env, value, fid);
                    } else {
                        optval.ling.l_onoff = 0;
                        optval.ling.l_linger = 0;
                    }
                    optlen = sizeof(optval.ling);
                } else {
                    optval.i = (*env)->GetIntField(env, value, fid);
                    optlen = sizeof(optval.i);
                }

                break;
            }

        /* Boolean -> int */
        default :
            optval.i = (on ? 1 : 0);
            optlen = sizeof(optval.i);

    }

    if (NET_SetSockOpt(fd, level, optname, (const void *)&optval, optlen) < 0) {
#ifdef __solaris__
        if (errno == EINVAL) {
            // On Solaris setsockopt will set errno to EINVAL if the socket
            // is closed. The default error message is then confusing
            char fullMsg[128];
            jio_snprintf(fullMsg, sizeof(fullMsg), "Invalid option or socket reset by remote peer");
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", fullMsg);
            return;
        }
#endif /* __solaris__ */
        NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException",
                                      "Error setting socket option");
    }
}

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketGetOption
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
PlainSocketImpl_socketGetOption(JNIEnv *env, jobject this,
                                              jint cmd, jobject iaContainerObj) {

    int fd;
    int level, optname, optlen;
    union {
        int i;
        struct linger ling;
    } optval;

    /*
     * Check that socket hasn't been closed
     */
    fd = getFD(env, this);
    if (fd < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Socket closed");
        return -1;
    }

    /*
     * SO_BINDADDR isn't a socket option
     */
    if (cmd == java_net_SocketOptions_SO_BINDADDR) {
        SOCKADDR him;
        socklen_t len = 0;
        int port;
        jobject iaObj;
        jclass iaCntrClass;
        jfieldID iaFieldID;

        len = SOCKADDR_LEN;

        if (getsockname(fd, (struct sockaddr *)&him, &len) < 0) {
            NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException",
                             "Error getting socket name");
            return -1;
        }
        iaObj = NET_SockaddrToInetAddress(env, (struct sockaddr *)&him, &port);
        CHECK_NULL_RETURN(iaObj, -1);

        iaCntrClass = (*env)->GetObjectClass(env, iaContainerObj);
        iaFieldID = (*env)->GetFieldID(env, iaCntrClass, "addr", "Ljava/net/InetAddress;");
        CHECK_NULL_RETURN(iaFieldID, -1);
        (*env)->SetObjectField(env, iaContainerObj, iaFieldID, iaObj);
        return 0; /* notice change from before */
    }

    /*
     * Map the Java level socket option to the platform specific
     * level and option name.
     */
    if (NET_MapSocketOption(cmd, &level, &optname)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Invalid option");
        return -1;
    }

    /*
     * Args are int except for SO_LINGER
     */
    if (cmd == java_net_SocketOptions_SO_LINGER) {
        optlen = sizeof(optval.ling);
    } else {
        optlen = sizeof(optval.i);
    }

    if (NET_GetSockOpt(fd, level, optname, (void *)&optval, &optlen) < 0) {
        NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException",
                                      "Error getting socket option");
        return -1;
    }

    switch (cmd) {
        case java_net_SocketOptions_SO_LINGER:
            return (optval.ling.l_onoff ? optval.ling.l_linger: -1);

        case java_net_SocketOptions_SO_SNDBUF:
        case java_net_SocketOptions_SO_RCVBUF:
        case java_net_SocketOptions_IP_TOS:
            return optval.i;

        default :
            return (optval.i == 0) ? -1 : 1;
    }
}


static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(PlainSocketImpl, socketGetOption, "(ILjava/lang/Object;)I"),
  NATIVE_METHOD(PlainSocketImpl, socketSetOption0, "(IZLjava/lang/Object;)V"),
};

void register_java_net_PlainSocketImpl(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/net/PlainSocketImpl", gMethods, NELEM(gMethods));
  PlainSocketImpl_initProto(env);
}
