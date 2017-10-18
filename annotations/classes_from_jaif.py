#!/usr/bin/env python

"""Print out list of java classes targeted by a jaif file."""

import os

PACKAGE_STRING = 'package '
CLASS_STRING = 'class '

current_package = None
with open(os.sys.argv[1], 'r') as jaif_file:
  for line in jaif_file:
    if line.startswith(PACKAGE_STRING):
      current_package = line[len(PACKAGE_STRING): line.find(':')]
    if line.startswith(CLASS_STRING) && current_package is not None:
      current_class = line[len(CLASS_STRING): line.find(':')]
      print current_package.replace('.', '/') + '/' + current_class + '.java'

os.sys.exit(0)
