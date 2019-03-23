#
# Copyright (C) 2014 Wind River Systems, Inc.
#
SUMMARY = "An implementation of the JSON specification according to RFC 4627"
DESCRIPTION = "An implementation of the JSON specification according to RFC 4627"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0;md5=801f80980d171dd6425610833a22dbe6"

PR = "r0"

BPV = "1.8.6"
PV = "${BPV}"
SRCREV = "7f4cfd853f2c919d854fb95548a19980feff17e8"

S = "${WORKDIR}/git"

SRC_URI = " \
    git://github.com/flori/json.git;protocol=https;branch=v1.8 \
    "

inherit ruby

DEPENDS += " \
    ruby \
    virtual/crypt \
"

ruby_do_compile() {
        EXTCONF_FILES=$(find . -name extconf.rb -exec ls {} \;)
        for e in $EXTCONF_FILES
        do
            if [ -f $e -a ! -f $e.orig ] ; then
                grep create_makefile $e > append2 || (exit 0)
                ruby_gen_extconf_fix
                cp $e $e.orig
                # Patch extconf.rb for cross compile
                cat append >> $e
            fi
        done
        for gem in ${RUBY_BUILD_GEMS}; do
                ${RUBY_COMPILE_FLAGS} gem build $gem
        done
        for e in $EXTCONF_FILES
        do
            if [ -f $e.orig ] ; then
                cat $e
                mv $e.orig $e
            fi
        done
}


ruby_gen_extconf_fix() {
        cat<<EOF>append
  RbConfig::MAKEFILE_CONFIG['CPPFLAGS'] = ENV['CPPFLAGS'] if ENV['CPPFLAGS']
  \$CPPFLAGS = ENV['CPPFLAGS'] if ENV['CPPFLAGS']
  RbConfig::MAKEFILE_CONFIG['CC'] = ENV['CC'] if ENV['CC']
  RbConfig::MAKEFILE_CONFIG['LD'] = ENV['LD'] if ENV['LD']
  RbConfig::MAKEFILE_CONFIG['CFLAGS'] = ENV['CFLAGS'] if ENV['CFLAGS']
  RbConfig::MAKEFILE_CONFIG['CXXFLAGS'] = ENV['CXXFLAGS'] if ENV['CXXFLAGS']
EOF
        cat append2>>append
        sysroot_ruby=${STAGING_INCDIR}/ruby-${RUBY_GEM_VERSION}
        ruby_arch=`ls -1 ${sysroot_ruby} |grep -v ruby |tail -1 2> /dev/null`
        cat<<EOF>>append
  system("perl -p -i -e 's#^topdir.*#topdir = ${sysroot_ruby}#' Makefile 2>&1 > /tmp/jjjjjj")
  system("perl -p -i -e 's#^topdir.*#topdir = ${sysroot_ruby}#' Makefile")
  system("perl -p -i -e 's#^hdrdir.*#hdrdir = ${sysroot_ruby}#' Makefile")
  system("perl -p -i -e 's#^arch_hdrdir.*#arch_hdrdir = ${sysroot_ruby}/\\\\\$(arch)#' Makefile")
  system("perl -p -i -e 's#^arch =.*#arch = ${ruby_arch}#' Makefile")
  system("perl -p -i -e 's#^LIBPATH =.*#LIBPATH = -L.#' Makefile")
  system("perl -p -i -e 's#^dldflags =.*#dldflags = ${LDFLAGS}#' Makefile")
  system("perl -p -i -e 's#^ldflags  =.*#ldflags = -L${STAGING_LIBDIR}#' Makefile")
EOF
}


BBCLASSEXTEND = "native"
