#!/usr/bin/env python

"""Generate annotated_java_files.bp from a jaif file."""
import os

PACKAGE_STRING = 'package '
CLASS_STRING = 'class '
SRC_PREFIX = 'ojluni/src/main/java/'

BP_TEMPLATE = '''filegroup {
    name: "annotated_ojluni_files",
    export_to_make_var: "annotated_ojluni_files",
    srcs: [
%s
    ],
}'''

srcs_list = []
current_package = None
with open(os.sys.argv[1], 'r') as jaif_file:
  for line in jaif_file:
    if line.startswith(PACKAGE_STRING):
      current_package = line[len(PACKAGE_STRING): line.find(':')]
    if line.startswith(CLASS_STRING) and current_package is not None:
      current_class = line[len(CLASS_STRING): line.find(':')]
      srcs_list.append(SRC_PREFIX + current_package.replace('.', '/') + '/' + current_class + '.java')

print '// Do not edit; generated using libcore/annotations/generate_annotated_java_files.py'
print BP_TEMPLATE % ('\n'.join(['        "' + src_entry + '",' for src_entry in sorted(srcs_list)]),)
os.sys.exit(0)
