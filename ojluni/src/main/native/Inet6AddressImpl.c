/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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
#include <sys/time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <string.h>
#include <strings.h>
#include <stdlib.h>
#include <ctype.h>
#ifdef _ALLBSD_SOURCE
#include <unistd.h> /* gethostname */
#endif

#include "jvm.h"
#include "jni_util.h"
#include "net_util.h"
#ifndef IPV6_DEFS_H
#include <netinet/icmp6.h>
#endif

#include "JNIHelp.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

/* the initial size of our hostent buffers */
#ifndef NI_MAXHOST
#define NI_MAXHOST 1025
#endif


/************************************************************************
 * Inet6AddressImpl
 */

static jclass ni_iacls;
static jclass ni_ia4cls;
static jclass ni_ia6cls;
static jmethodID ni_ia4ctrID;
static jmethodID ni_ia6ctrID;

/*
 * Class:     java_net_Inet6AddressImpl
 * Method:    getHostByAddr
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Inet6AddressImpl_getHostByAddr0(JNIEnv *env, jobject this,
                                             jbyteArray addrArray) {

    jstring ret = NULL;

#ifdef AF_INET6
    char host[NI_MAXHOST+1];
    int error = 0;
    int len = 0;
    jbyte caddr[16];

    if (NET_addrtransAvailable()) {
        struct sockaddr_in him4;
        struct sockaddr_in6 him6;
        struct sockaddr *sa;

        /*
         * For IPv4 addresses construct a sockaddr_in structure.
         */
        if ((*env)->GetArrayLength(env, addrArray) == 4) {
            jint addr;
            (*env)->GetByteArrayRegion(env, addrArray, 0, 4, caddr);
            addr = ((caddr[0]<<24) & 0xff000000);
            addr |= ((caddr[1] <<16) & 0xff0000);
            addr |= ((caddr[2] <<8) & 0xff00);
            addr |= (caddr[3] & 0xff);
            memset((void *) &him4, 0, sizeof(him4));
            him4.sin_addr.s_addr = (uint32_t) htonl(addr);
            him4.sin_family = AF_INET;
            sa = (struct sockaddr *) &him4;
            len = sizeof(him4);
        } else {
            /*
             * For IPv6 address construct a sockaddr_in6 structure.
             */
            (*env)->GetByteArrayRegion(env, addrArray, 0, 16, caddr);
            memset((void *) &him6, 0, sizeof(him6));
            memcpy((void *)&(him6.sin6_addr), caddr, sizeof(struct in6_addr) );
            him6.sin6_family = AF_INET6;
            sa = (struct sockaddr *) &him6 ;
            len = sizeof(him6) ;
        }

        error = (*getnameinfo_ptr)(sa, len, host, NI_MAXHOST, NULL, 0,
                                   NI_NAMEREQD);

        if (!error) {
            ret = (*env)->NewStringUTF(env, host);
        }
    }
#endif /* AF_INET6 */

    if (ret == NULL) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "UnknownHostException", NULL);
    }

    return ret;
}

#define SET_NONBLOCKING(fd) {           \
        int flags = fcntl(fd, F_GETFL); \
        flags |= O_NONBLOCK;            \
        fcntl(fd, F_SETFL, flags);      \
}

static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(Inet6AddressImpl, getHostByAddr0, "([B)Ljava/lang/String;"),
};

void register_java_net_Inet6AddressImpl(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/net/Inet6AddressImpl", gMethods, NELEM(gMethods));
}
