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

"""Common Utils."""


# pylint: disable=g-importing-member
from dataclasses import dataclass
from pathlib import Path
import sys
from typing import List

# pylint: disable=g-import-not-at-top
try:
  from git import Tree
except ModuleNotFoundError:
  print(
      'ERROR: Please install GitPython by `pip3 install GitPython`.',
      file=sys.stderr)
  exit(1)

THIS_DIR = Path(__file__).resolve().parent
LIBCORE_DIR = THIS_DIR.parent.parent.resolve()


@dataclass
class ExpectedUpstreamEntry:
  """A map entry in the EXPECTED_UPSTREAM file."""
  dst_path: str  # destination path
  git_ref: str  # a git reference to an upstream commit
  src_path: str  # source path in the commit pointed by the git_ref
  comment_lines: str = ''  # The comment lines above the entry line


class ExpectedUpstreamFile:
  """A file object representing the EXPECTED_UPSTREAM file."""

  def __init__(self, file_path: str = LIBCORE_DIR / 'EXPECTED_UPSTREAM'):
    self.path = Path(file_path)

  def read_all_entries(self) -> List[ExpectedUpstreamEntry]:
    """Read all entries from the file."""
    result: List[ExpectedUpstreamEntry] = []
    with self.path.open() as file:
      comment_lines = ''  # Store the comment lines in the next entry
      for line in file:
        stripped = line.strip()
        # Ignore empty lines and comments starting with '#'
        if not stripped or stripped.startswith('#'):
          comment_lines += line
          continue

        entry = self.parse_line(stripped, comment_lines)
        result.append(entry)
        comment_lines = ''

    return result

  def write_all_entries(self, entries: List[ExpectedUpstreamEntry]) -> None:
    """Write all entries into the file."""
    with self.path.open('w') as file:
      for e in entries:
        file.write(e.comment_lines)
        file.write(','.join([e.dst_path, e.git_ref, e.src_path]))
        file.write('\n')

  def write_new_entry(self,
                      entry: ExpectedUpstreamEntry,
                      entries: List[ExpectedUpstreamEntry] = None) -> None:
    if entries is None:
      entries = self.read_all_entries()

    entries.append(entry)
    self.sort_and_write_all_entries(entries)

  def sort_and_write_all_entries(self,
                                 entries: List[ExpectedUpstreamEntry]) -> None:
    header = entries[0].comment_lines
    entries[0].comment_lines = ''
    entries.sort(key=lambda e: e.dst_path)
    # Keep the header above the first entry
    entries[0].comment_lines = header + entries[0].comment_lines
    self.write_all_entries(entries)

  @staticmethod
  def parse_line(line: str, comment_lines: str) -> ExpectedUpstreamEntry:
    items = line.split(',')
    size = len(items)
    if size != 3:
      raise ValueError(
          f"The size must be 3, but is {size}. The line is '{line}'")

    return ExpectedUpstreamEntry(items[0], items[1], items[2], comment_lines)


def has_file_in_tree(path: str, tree: Tree) -> bool:
  """Returns True if the directory / file exists in the tree."""
  try:
    # pylint: disable=pointless-statement
    tree[path]
    return True
  except KeyError:
    return False
