# -*- mode: makefile -*-
# Copyright (C) 2007 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# Build jaif-annotated source files for ojluni target .
#
ojluni_annotate_dir := $(call intermediates-dir-for,JAVA_LIBRARIES,core-oj,,COMMON)/annotated
ojluni_annotate_target := $(ojluni_annotate_dir)/timestamp
ojluni_annotate_jaif := $(LOCAL_PATH)/annotations/ojluni.jaif
ojluni_annotate_input := $(annotated_ojluni_files)
ojluni_annotate_output := $(patsubst $(LOCAL_PATH)/ojluni/src/main/java/%, $(ojluni_annotate_dir)/%, $(ojluni_annotate_input))

$(ojluni_annotate_target): PRIVATE_ANNOTATE_TARGET := $(ojluni_annotate_target)
$(ojluni_annotate_target): PRIVATE_ANNOTATE_DIR := $(ojluni_annotate_dir)
$(ojluni_annotate_target): PRIVATE_ANNOTATE_JAIF := $(ojluni_annotate_jaif)
$(ojluni_annotate_target): PRIVATE_ANNOTATE_INPUT := $(ojluni_annotate_input)
$(ojluni_annotate_target): PRIVATE_ANNOTATE_GENERATE_CMD := $(LOCAL_PATH)/annotations/generate_annotated_java_files.py
$(ojluni_annotate_target): PRIVATE_ANNOTATE_GENERATE_OUTPUT := $(LOCAL_PATH)/annotated_java_files.bp
$(ojluni_annotate_target): PRIVATE_INSERT_ANNOTATIONS_TO_SOURCE := external/annotation-tools/annotation-file-utilities/scripts/insert-annotations-to-source

# Diff output of _ojluni_annotate_generate_cmd with what we have, and if generate annotated source.
$(ojluni_annotate_target):  $(ojluni_annotate_input) $(ojluni_annotate_jaif)
	rm -rf $(PRIVATE_ANNOTATE_DIR)
	mkdir -p $(PRIVATE_ANNOTATE_DIR)
	$(PRIVATE_ANNOTATE_GENERATE_CMD) $(PRIVATE_ANNOTATE_JAIF) > $(PRIVATE_ANNOTATE_DIR)/annotated_java_files.bp.tmp
	diff -u $(PRIVATE_ANNOTATE_GENERATE_OUTPUT) $(PRIVATE_ANNOTATE_DIR)/annotated_java_files.bp.tmp || \
	(echo -e "********************" >&2; \
	 echo -e "annotated_java_files.bp needs regenerating. Please run:" >&2; \
	 echo -e "libcore/annotations/generate_annotated_java_files.py libcore/annotations/ojluni.jaif > libcore/annotated_java_files.bp" >&2; \
	 echo -e "********************" >&2; exit 1)
	rm $(PRIVATE_ANNOTATE_DIR)/annotated_java_files.bp.tmp
	$(PRIVATE_INSERT_ANNOTATIONS_TO_SOURCE) -d $(PRIVATE_ANNOTATE_DIR) $(PRIVATE_ANNOTATE_JAIF) $(PRIVATE_ANNOTATE_INPUT)
	touch $@
$(ojluni_annotate_target): .KATI_IMPLICIT_OUTPUTS := $(ojluni_annotate_output)

ojluni_annotate_dir:=
ojluni_annotate_target:=
ojluni_annotate_jaif:=
ojluni_annotate_input:=
ojluni_annotate_output:=


# Archive a copy of the classes.jar in SDK build.
full_classes_jar := $(call intermediates-dir-for,JAVA_LIBRARIES,core.current.stubs,,COMMON)/classes.jar
$(call dist-for-goals,sdk win_sdk,$(full_classes_jar):core.current.stubs.jar)
