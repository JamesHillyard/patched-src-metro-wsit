#!/bin/sh
USAGE="Usage: migrate-core.sh [-hvf]"

# parse command line arguments
while getopts 'hvf' OPT; do
    case "$OPT" in
	h)  echo $USAGE
            exit 0
            ;;
	v)  VERBOSE="-v"
            ;;
	f)  FORCE_RM_FLAG="-f"
            ;;
	?)  # all other characters - error
            echo $USAGE >&2
            exit 1
            ;;
    esac
    shift
done
shift


# access additional parameters through $@ or $* as usual or using this loop:
# for PARAM; do
#    echo $PARAM
# done

MODULE_ROOT="$NEW_PROJECT_ROOT/metro-core"
SRC_ARTIFACTS="com/sun/xml/ws/assembler/*:com/sun/xml/ws/dump/*"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
POM_TEMPLATE=""

source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT $POM_TEMPLATE
source ./move-sources.sh  -n $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS