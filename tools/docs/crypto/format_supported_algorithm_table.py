#!/usr/bin/env python
#
# Copyright (C) 2017 The Android Open Source Project
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

"""Outputs HTML based on an input JSON file.

Outputs HTML tables suitable for inclusion in the Android documentation that
reflect the crypto algorithm support shown in the provided data file.
"""

import operator
import sys

import crypto_docs


def sort_by_name(seq):
    return sorted(seq, key=lambda x: x['name'])


def main():
    if len(sys.argv) < 2:
        print 'Must supply argument to data file.'
        sys.exit(1)
    data = crypto_docs.load_json(sys.argv[1])
    categories = sort_by_name(data['categories'])
    print '<h2 id="SupportedAlgorithms">Supported Algorithms</h2>'
    print
    print '<ul>'
    for category in categories:
        print ('  <li><a href="#Supported{name}">'
               '<code>{name}</code></a></li>'.format(**category))
    print '</ul>'
    for category in categories:
        if category['name'] == 'Cipher':
            # We display ciphers in a four-column table to conserve space and
            # so that it's more comprehensible.  To do this, we have to
            # collapse all our ciphers into "equivalence classes" of a sort.

            # First, collect the relevant data for each algorithm into a tuple.
            # The mode and padding are in lists because we are going to collapse
            # multiple tuples with those in later steps.
            algorithms = sort_by_name(category['algorithms'])
            tuples = []
            for algorithm in algorithms:
                name, mode, padding = algorithm['name'].split('/')
                tuples.append((
                    name,
                    [mode],
                    [padding],
                    algorithm['supported_api_levels'],
                    'deprecated' in algorithm and algorithm['deprecated']))
            # Sort the tuples by all items except padding, then collapse
            # items with all non-padding values the same (which will always be
            # neighboring items) into a single item.
            tuples.sort(key=operator.itemgetter(0, 1, 3, 4))
            i = 0
            while i < len(tuples) - 1:
                if (tuples[i][0] == tuples[i+1][0]
                    and tuples[i][1] == tuples[i+1][1]
                    and tuples[i][3] == tuples[i+1][3]
                    and tuples[i][4] == tuples[i+1][4]):
                    tuples[i][2].extend(tuples[i+1][2])
                    del tuples[i+1]
                else:
                    i += 1
            # Do the same thing as above, but with modes.
            tuples.sort(key=operator.itemgetter(0, 2, 3, 4))
            i = 0
            while i < len(tuples) - 1:
                if (tuples[i][0] == tuples[i+1][0]
                    and tuples[i][2] == tuples[i+1][2]
                    and tuples[i][3] == tuples[i+1][3]
                    and tuples[i][4] == tuples[i+1][4]):
                    tuples[i][1].extend(tuples[i+1][1])
                    del tuples[i+1]
                else:
                    i += 1
            # Display the table with rowspans for those entries where all the
            # items have the same algorithm, mode, etc
            print '''
<h3 id="Supported{name}">{name}</h3>
<table>
  <thead>
    <th>Algorithm</th>
    <th>Modes</th>
    <th>Paddings</th>
    <th>Supported API Levels</th>
  </thead>
  <tbody>'''.format(**category)
            tuples.sort(key=operator.itemgetter(0, 4, 1, 2, 3))
            i = 0
            cur_deprecated = None
            cur_algorithm = None
            cur_mode = None
            while i < len(tuples):
                row = tuples[i]
                if row[4] != cur_deprecated:
                    cur_deprecated = row[4]
                    cur_algorithm = None
                    cur_mode = None
                if cur_deprecated:
                    print '    <tr class="deprecated">'
                else:
                    print '    <tr>'
                if row[0] != cur_algorithm:
                    cur_algorithm = row[0]
                    cur_mode = None
                    j = i + 1
                    while (j < len(tuples)
                           and tuples[j][4] == cur_deprecated
                           and tuples[j][0] == cur_algorithm):
                        j += 1
                    rowspan = j - i
                    if rowspan > 1:
                        print '      <td rowspan="%d">%s</td>' % (rowspan, cur_algorithm)
                    else:
                        print '      <td>%s</td>' % cur_algorithm
                if row[1] != cur_mode:
                    cur_mode = row[1]
                    j = i + 1
                    while (j < len(tuples)
                           and tuples[j][4] == cur_deprecated
                           and tuples[j][0] == cur_algorithm
                           and tuples[j][1] == cur_mode):
                        j += 1
                    rowspan = j - i
                    modestring = '<br>'.join(cur_mode)
                    if rowspan > 1:
                        print '      <td rowspan="%d">%s</td>' % (rowspan, modestring)
                    else:
                        print '      <td>%s</td>' % modestring
                print '      <td>%s</td>' % '<br>'.join(row[2])
                print '      <td>%s</td>' % row[3]
                print '    </tr>'
                i += 1
            print '''
  </tbody>
</table>'''
        else:
            print '''
<h3 id="Supported{name}">{name}</h3>
<table>
  <thead>
    <th>Algorithm</th>
    <th>Supported API Levels</th>
  </thead>
  <tbody>'''.format(**category)
            algorithms = sort_by_name(category['algorithms'])
            for algorithm in algorithms:
                dep_class = ''
                if 'deprecated' in algorithm and algorithm['deprecated']:
                    dep_class = ' class="deprecated"'
                print '''
    <tr{deprecated_class}>
      <td>{name}</td>
      <td>{supported_api_levels}</td>
    </tr>'''.format(deprecated_class=dep_class, **algorithm)
            print '''
  </tbody>
</table>'''


if __name__ == '__main__':
    main()
