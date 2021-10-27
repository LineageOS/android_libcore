This folder contains tools to update the files in the aosp/expected_upstream
branch.

# Prerequisite
* python3
* pip3
* A remote `aosp` is setup in your local git repository

# Directory Layout
1. ojluni/
    * It has the same layout as the ojluni/ files in aosp/master
    * A file should only exist if aosp/master has the such file path, and the
    file content comes from the OpenJDK upstream.
2. EXPECTED_UPSTREAM file
    * The file format is like .csv file using a `,` separator
    * The table has 3 columns, i.e.
        1. Destination path in ojluni/
        2. Expected upstream version. Normally, it's a git tag in the upstream
        git repositories.
        3. File path in the git tree specified in the 2nd column.
3. tools/expected_upstream/
    * Contains the tools

# Tools
## tools/expected_upstream/install_tools.sh
* Installs the dependency libraries
* Installs the other tools into your current shell process

## ojluni_modify_expectation
* Command line tool that can help modify the EXPECTED_UPSTREAM file

## ojluni_refresh_files
* Reads the EXPECTED_UPSTREAM file and updates the files contents in ojluni/
accordingly

# Workflow in command lines
## Setup
1. Switch to the expected_upstream branch
```shell
git branch local_expected_upstream aosp/expected_upstream
git checkout local_expected_upstream
```

2. Install tools
```shell
source ./tools/expected_upstream/install_tools.sh
```
## Upgrade a java class to a higher OpenJDK version
For example, upgrade `java.lang.String` to 11+28 version:

```shell
ojluni_modify_expectation modify java.lang.String jdk11u/jdk-11+28
ojluni_refresh_files
```

or if `java.lang.String` is missing in EXPECTED_UPSTREAM:
```shell
ojluni_modify_expectation add jdk11u/jdk-11+28 java.lang.String
ojluni_refresh_files
```
2 commits should be created to update the `ojluni/src/main/java/java/lang/String.java`.
You can verify and view the diff by the following command

```shell
git diff aosp/expected_upstream -- ojluni/src/main/java/java/lang/String.java
```

You can then upload your change to AOSP gerrit.
```shell
repo upload --cbr -t . # -t sets a topic to the CLs in the gerrit
```

Remember to commit your EXPECTED_UPSTREAM file change into a new commit
```shell
git commit -- EXPECTED_UPSTREAM
```

Then upload your change to AOSP gerrit.
```shell
repo upload --cbr -t . # -t sets a topic to the CLs in the gerrit
```

Then you can switch back to your local `master` branch to apply the changes
```shell
git checkout <local_master_branch>
git merge local_expected_upstream
# Resolve any merge conflict
git commit --amend # Amend the commit message and add the bug number you are working on
repo upload .
```

## Add a java test from the upstream

The process is similar to the above commands, but needs to run
`ojluni_modify_expectation` with an `add` subcommand.

For example, add a test for `String.isEmpty()` method:
```shell
ojluni_modify_expectation add jdk8u/jdk8u121-b13 java.lang.String.IsEmpty
```
Note: java.lang.String.IsEmpty is a test class in the upstream repository.


# Known bugs
* `repo upload` may not succeed because gerrit returns error.
    1. Just try to run `repo upload` again!
        * The initial upload takes a long time because it tries to sync with the
          remote AOSP gerrit server. The second upload is much faster and thus
          it may succeed.
    2. `repo upload` returns TimeOutException, but the CL has been uploaded.
       Just find your CL in http://r.android.com/. See http://b/202848945
    3. Try to upload the merge commits 1 by 1
    ```shell
    git rev-parse HEAD # a sha is printed and you will need it later
    git reset HEAD~1 # reset to a earlier commit
    repo upload --cbr . # try to upload it again
    git reset <the sha printed above>
    ```
* After `ojluni_modify_expectation add` and `ojluni_refresh_files`, a `git commit -a`
  would include more files than just EXPECTED_UPSTREAM, because `git`, e.g. `git status`,
  isn't aware of changes in the working tree / in the file system. This can lead to
  an error when checking out the branch that is based on master.
    1. Do a `git checkout --hard <initial commit before the add>`
    2. Rerun the `ojluni_modify_expectation add` and `ojluni_refresh_files`
    3. `git stash && git stash pop`
    4. Commit the updated EXPECTED_UPSTREAM and proceed

# Report bugs
* Report bugs if the git repository is corrupt!
    * Sometimes, you can recover the repository by running `git reset aosp/expected_upstream`
