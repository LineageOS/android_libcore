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

import json
import sys


def sort_by_name(seq):
    return sorted(seq, key=lambda x: x['name'])


def main():
    if len(sys.argv) < 2:
        print 'Must supply argument to data file.'
        sys.exit(1)
    data = json.load(open(sys.argv[1]))
    categories = sort_by_name(data['categories'])
    print '<h2 id="SupportedAlgorithms">Supported Algorithms</h2>'
    print
    print '<ul>'
    for category in categories:
        print ('  <li><a href="#Supported{name}">'
               '<code>{name}</code></a></li>'.format(**category))
    print '</ul>'
    for category in categories:
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
