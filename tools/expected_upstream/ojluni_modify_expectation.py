#!/usr/bin/python3 -B

# Copyright 2021 The Android Open Source Project
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
"""ojluni_modify_expectation is a command-line tool for modifying the EXPECTED_UPSTREAM file."""

import argparse
# pylint: disable=g-importing-member
from pathlib import PurePath
import sys
# pylint: disable=g-multiple-import
from typing import (
    Set,
    Sequence,
    List,
)

from common_util import (
    ExpectedUpstreamEntry,
    ExpectedUpstreamFile,
    LIBCORE_DIR,
    has_file_in_tree,
)

# Import git only after common_util because common_util will
# produce informative error
from git import (Blob, Commit, Repo)
from gitdb.exc import BadName

LIBCORE_REPO = Repo(LIBCORE_DIR.as_posix())

UPSTREAM_JAVA_BASE_PATHS = [
    'jdk/src/share/classes/',
    'src/java.base/share/classes/',
]

UPSTREAM_TEST_PATHS = [
    'jdk/test/',
    'test/jdk/',
]

UPSTREAM_SEARCH_PATHS = UPSTREAM_JAVA_BASE_PATHS + UPSTREAM_TEST_PATHS

OJLUNI_JAVA_BASE_PATH = 'ojluni/src/main/java/'
OJLUNI_TEST_PATH = 'ojluni/src/'

AUTOCOMPLETE_TAGS = [
    'jdk7u/jdk7u40-b60',
    'jdk8u/jdk8u121-b13',
    'jdk8u/jdk8u60-b31',
    'jdk9/jdk-9+181',
    'jdk11u/jdk-11+28',
    'jdk11u/jdk-11.0.13-ga',
]


def error_and_exit(msg: str) -> None:
  print(f'Error: {msg}', file=sys.stderr)
  sys.exit(1)


def get_commit_or_exit(git_ref: str) -> Commit:
  try:
    return LIBCORE_REPO.commit(git_ref)
  except BadName as e:
    error_and_exit(f'{e}')


def translate_from_class_name_to_ojluni_path(class_or_path: str) -> str:
  # if it contains '/', then it's a path
  if '/' in class_or_path:
    return class_or_path

  base_path = OJLUNI_TEST_PATH if class_or_path.startswith(
      'test.') else OJLUNI_JAVA_BASE_PATH

  relative_path = class_or_path.replace('.', '/')
  return f'{base_path}{relative_path}.java'


def translate_src_path_to_ojluni_path(src_path: str) -> str:
  """Returns None if the path can be translated into a ojluni/ path."""
  relative_path = None
  for base_path in UPSTREAM_TEST_PATHS:
    if src_path.startswith(base_path):
      length = len(base_path)
      relative_path = src_path[length:]
      break

  if relative_path:
    return f'{OJLUNI_TEST_PATH}test/{relative_path}'

  for base_path in UPSTREAM_JAVA_BASE_PATHS:
    if src_path.startswith(base_path):
      length = len(base_path)
      relative_path = src_path[length:]
      break

  if relative_path:
    return f'{OJLUNI_JAVA_BASE_PATH}{relative_path}'

  return None


def find_src_path_from_class(commit: Commit, class_or_path: str) -> str:
  """Finds a valid source path given a valid class name or path."""
  # if it contains '/', then it's a path
  if '/' in class_or_path:
    if has_file_in_tree(class_or_path, commit.tree):
      return class_or_path
    else:
      return None

  relative_path = class_or_path.replace('.', '/')
  src_path = None
  full_paths = []
  for base_path in UPSTREAM_SEARCH_PATHS:
    full_path = f'{base_path}{relative_path}.java'
    full_paths.append(full_path)
    if has_file_in_tree(full_path, commit.tree):
      src_path = full_path
      break

  return src_path


def find_src_path_from_ojluni_path(commit: Commit, ojluni_path: str) -> str:
  """Returns a source path that guessed from the ojluni_path."""
  base_paths = None
  relative_path = None
  if ojluni_path.startswith(OJLUNI_JAVA_BASE_PATH):
    base_paths = UPSTREAM_JAVA_BASE_PATHS
    length = len(OJLUNI_JAVA_BASE_PATH)
    relative_path = ojluni_path[length:]
  elif ojluni_path.startswith(OJLUNI_TEST_PATH):
    base_paths = UPSTREAM_TEST_PATHS
    length = len(OJLUNI_TEST_PATH)
    relative_path = ojluni_path[length:]
  else:
    return None

  for base_path in base_paths:
    full_path = base_path + relative_path
    if has_file_in_tree(full_path, commit.tree):
      return full_path

  return None


def autocomplete_existing_ojluni_path(input_path: str,
                                      existing_paths: List[str]) -> Set[str]:
  """Returns a set of existing file paths matching the given partial path."""
  path_matches = list(
      filter(lambda path: path.startswith(input_path), existing_paths))
  result_set: Set[str] = set()
  # if it's found, just return the result
  if input_path in path_matches:
    result_set.add(input_path)
  else:
    input_ojluni_path = PurePath(input_path)
    # the input ends with '/', the autocompletion result contain the children
    # instead of the matching the prefix in its parent directory
    input_path_parent_or_self = input_ojluni_path
    if not input_path.endswith('/'):
      input_path_parent_or_self = input_path_parent_or_self.parent
    n_parts = len(input_path_parent_or_self.parts)
    for match in path_matches:
      path = PurePath(match)
      # path.parts[n_parts] should not exceed the index and should be
      # a valid child path because input_path_parent_or_self must be a
      # valid directory
      child = list(path.parts)[n_parts]
      result = (input_path_parent_or_self / child).as_posix()
      # if result is not exact, the result represents a directory.
      if result != match:
        result += '/'
      result_set.add(result)

  return result_set


def convert_path_to_java_class_name(path: str, base_path: str) -> str:
  base_len = len(base_path)
  result = path[base_len:]
  if result.endswith('.java'):
    result = result[0:-5]
  result = result.replace('/', '.')
  return result


def autocomplete_existing_class_name(input_class_name: str,
                                     existing_paths: List[str]) -> List[str]:
  """Returns a list of package / class names given the partial class name."""
  # If '/' exists, it's probably a path, not a partial class name
  if '/' in input_class_name:
    return []

  result_list = []
  partial_relative_path = input_class_name.replace('.', '/')
  for base_path in [OJLUNI_JAVA_BASE_PATH, OJLUNI_TEST_PATH]:
    partial_ojluni_path = base_path + partial_relative_path
    result_paths = autocomplete_existing_ojluni_path(partial_ojluni_path,
                                                     existing_paths)
    # pylint: disable=cell-var-from-loop
    result_list.extend(
        map(lambda path: convert_path_to_java_class_name(path, base_path),
            list(result_paths)))

  return result_list


def autocomplete_tag_or_commit(str_tag_or_commit: str) -> List[str]:
  """Returns a list of tags / commits matching the given partial string."""
  if str_tag_or_commit is None:
    str_tag_or_commit = ''
  return list(
      filter(lambda tag: tag.startswith(str_tag_or_commit), AUTOCOMPLETE_TAGS))


def autocomplete_upstream_path(input_path: str, commit: Commit,
                               excluded_paths: Set[str]) -> List[str]:
  """Returns a list of source paths matching the given partial string."""
  result_list = []

  def append_if_not_excluded(path: str) -> None:
    nonlocal result_list, excluded_paths
    if path not in excluded_paths:
      result_list.append(path)

  search_tree = commit.tree
  path_obj = PurePath(input_path)
  is_exact = has_file_in_tree(path_obj.as_posix(), search_tree)
  search_word = ''
  if is_exact:
    git_obj = search_tree[path_obj.as_posix()]
    if isinstance(git_obj, Blob):
      append_if_not_excluded(input_path)
      return result_list
    else:
      # git_obj is a tree
      search_tree = git_obj
  elif len(path_obj.parts) >= 2:
    parent_path = path_obj.parent.as_posix()
    if has_file_in_tree(parent_path, search_tree):
      search_tree = search_tree[parent_path]
      search_word = path_obj.name
    else:
      # Return empty list because no such path is found
      return result_list
  else:
    search_word = input_path

  for tree in search_tree.trees:
    tree_path = PurePath(tree.path)
    if tree_path.name.startswith(search_word):
      append_if_not_excluded(tree.path)

  for blob in search_tree.blobs:
    blob_path = PurePath(blob.path)
    if blob_path.name.startswith(search_word):
      append_if_not_excluded(blob.path)

  return result_list


def autocomplete_upstream_class(input_class_name: str, commit: Commit,
                                excluded_paths: Set[str]) -> List[str]:
  """Return a list of package / class names from given commit and input."""
  # If '/' exists, it's probably a path, not a class name.
  if '/' in input_class_name:
    return []

  result_list = []
  for base_path in UPSTREAM_SEARCH_PATHS:
    base_len = len(base_path)
    path = base_path + input_class_name.replace('.', '/')
    path_results = autocomplete_upstream_path(path, commit, excluded_paths)
    for p in path_results:
      relative_path = p[base_len:]
      if relative_path.endswith('.java'):
        relative_path = relative_path[0:-5]
      result_list.append(relative_path.replace('/', '.'))

  return result_list


COMMAND_ACTIONS = ['add', 'modify', 'sort']


def autocomplete_action(partial_str: str) -> None:
  result_list = list(
      filter(lambda action: action.startswith(partial_str), COMMAND_ACTIONS))
  print('\n'.join(result_list))
  exit(0)


def main(argv: Sequence[str]) -> None:
  is_auto_complete = len(argv) >= 2 and argv[0] == '--autocomplete'
  # argparse can't help autocomplete subcommand. We implement this without
  # argparse here.
  if is_auto_complete and argv[1] == '1':
    action = argv[2] if len(argv) >= 3 else ''
    autocomplete_action(action)

  # If it's for autocompletion, then all arguments are optional.
  parser_nargs = '?' if is_auto_complete else 1

  main_parser = argparse.ArgumentParser(
      description='A command line tool modifying the EXPECTED_UPSTREAM file.')
  # --autocomplete <int> is an 'int' argument because the value represents
  # the raw index of the argument to be autocompleted received in the Shell,
  # and this number is not always the same as the number of arguments
  # received here, i.e. len(argv), for examples of empty value in the
  # argument or autocompleting the middle argument, not last argument.
  main_parser.add_argument(
      '--autocomplete', type=int, help='flag when tabbing in command line')
  subparsers = main_parser.add_subparsers(
      dest='command', help='sub-command help')

  add_parser = subparsers.add_parser(
      'add', help='Add a new entry into the EXPECTED_UPSTREAM '
      'file')
  add_parser.add_argument(
      'tag_or_commit',
      nargs=parser_nargs,
      help='A git tag or commit in the upstream-openjdkXXX branch')
  add_parser.add_argument(
      'class_or_source_file',
      nargs=parser_nargs,
      help='Fully qualified class name or upstream source path')
  add_parser.add_argument(
      'ojluni_path', nargs='?', help='Destination path in ojluni/')

  modify_parser = subparsers.add_parser(
      'modify', help='Modify an entry in the EXPECTED_UPSTREAM file')
  modify_parser.add_argument(
      'class_or_ojluni_path', nargs=parser_nargs, help='File path in ojluni/')
  modify_parser.add_argument(
      'tag_or_commit',
      nargs=parser_nargs,
      help='A git tag or commit in the upstream-openjdkXXX branch')
  modify_parser.add_argument(
      'source_file', nargs='?', help='A upstream source path')

  subparsers.add_parser(
      'sort', help='Sort the entries in the EXPECTED_UPSTREAM file')

  args = main_parser.parse_args(argv)

  expected_upstream_file = ExpectedUpstreamFile()
  expected_entries = expected_upstream_file.read_all_entries()

  if is_auto_complete:
    no_args = args.autocomplete

    autocomp_result = []
    if args.command == 'modify':
      if no_args == 2:
        input_class_or_ojluni_path = args.class_or_ojluni_path
        if input_class_or_ojluni_path is None:
          input_class_or_ojluni_path = ''

        existing_dst_paths = list(
            map(lambda entry: entry.dst_path, expected_entries))
        # Case 1: Treat the input as file path
        autocomp_result += autocomplete_existing_ojluni_path(
            input_class_or_ojluni_path, existing_dst_paths)

        # Case 2: Treat the input as java package / class name
        autocomp_result += autocomplete_existing_class_name(
            input_class_or_ojluni_path, existing_dst_paths)
      elif no_args == 3:
        autocomp_result += autocomplete_tag_or_commit(args.tag_or_commit)
    elif args.command == 'add':
      if no_args == 2:
        autocomp_result += autocomplete_tag_or_commit(args.tag_or_commit)
      elif no_args == 3:
        commit = get_commit_or_exit(args.tag_or_commit)
        class_or_src_path = args.class_or_source_file
        if class_or_src_path is None:
          class_or_src_path = ''

        existing_src_paths = set(map(lambda e: e.src_path, expected_entries))
        autocomp_result += autocomplete_upstream_path(class_or_src_path, commit,
                                                      existing_src_paths)

        autocomp_result += autocomplete_upstream_class(class_or_src_path,
                                                       commit,
                                                       existing_src_paths)

    print('\n'.join(autocomp_result))
    exit(0)

  if args.command == 'modify':
    dst_class_or_file = args.class_or_ojluni_path[0]
    dst_file = translate_from_class_name_to_ojluni_path(dst_class_or_file)
    matches = list(filter(lambda e: dst_file == e.dst_path, expected_entries))
    if not matches:
      error_and_exit(f'{dst_file} is not found in the EXPECTED_UPSTREAM.')
    entry: ExpectedUpstreamEntry = matches[0]
    str_tag_or_commit = args.tag_or_commit[0]
    is_src_given = args.source_file is not None
    src_path = args.source_file if is_src_given else entry.src_path
    commit = get_commit_or_exit(str_tag_or_commit)
    if has_file_in_tree(src_path, commit.tree):
      pass
    elif not is_src_given:
      guessed_src_path = find_src_path_from_ojluni_path(commit, dst_file)
      if guessed_src_path is None:
        error_and_exit('[source_file] argument is required.')
      src_path = guessed_src_path
    else:
      error_and_exit(f'{src_path} is not found in the {str_tag_or_commit}')
    entry.git_ref = str_tag_or_commit
    entry.src_path = src_path
    expected_upstream_file.write_all_entries(expected_entries)
    print(f'Modified the entry {entry}')
  elif args.command == 'add':
    class_or_src_path = args.class_or_source_file[0]
    str_tag_or_commit = args.tag_or_commit[0]
    commit = get_commit_or_exit(str_tag_or_commit)
    src_path = find_src_path_from_class(commit, class_or_src_path)
    if src_path is None:
      error_and_exit(f'{class_or_src_path} is not found in {commit}. '
                     f'The search paths are:\n{UPSTREAM_SEARCH_PATHS}')
    ojluni_path = args.ojluni_path
    # Guess the source path if it's not given in the argument
    if ojluni_path is None:
      ojluni_path = translate_src_path_to_ojluni_path(src_path)
    if ojluni_path is None:
      error_and_exit('The ojluni destination path is not given.')

    matches = list(
        filter(lambda e: ojluni_path == e.dst_path, expected_entries))
    if matches:
      error_and_exit(f"Can't add the file {ojluni_path} because "
                     f'{class_or_src_path} exists in the EXPECTED_UPSTREAM')

    new_entry = ExpectedUpstreamEntry(ojluni_path, str_tag_or_commit, src_path)
    expected_upstream_file.write_new_entry(new_entry, expected_entries)
  elif args.command == 'sort':
    expected_upstream_file.sort_and_write_all_entries(expected_entries)
  else:
    error_and_exit(f'Unknown subcommand: {args.command}')


if __name__ == '__main__':
  main(sys.argv[1:])
