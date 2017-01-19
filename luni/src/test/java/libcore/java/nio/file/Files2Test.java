/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package libcore.java.nio.file;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
import static junit.framework.TestCase.assertTrue;
import static libcore.java.nio.file.FilesSetup.DATA_FILE;
import static libcore.java.nio.file.FilesSetup.NON_EXISTENT_FILE;
import static libcore.java.nio.file.FilesSetup.execCmdAndWaitForTermination;
import static libcore.java.nio.file.FilesSetup.readFromInputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Files2Test {
    @Rule
    public FilesSetup filesSetup = new FilesSetup();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private Path mockPath;
    @Mock
    private Path mockPath2;
    @Mock
    private FileSystem mockFileSystem;
    @Mock
    private FileSystemProvider mockFileSystemProvider;

    @Before
    public void setUp() throws Exception {
        when(mockPath.getFileSystem()).thenReturn(mockFileSystem);
        when(mockPath2.getFileSystem()).thenReturn(mockFileSystem);
        when(mockFileSystem.provider()).thenReturn(mockFileSystemProvider);
    }

    @Test
    public void test_move() throws IOException {
        CopyOption mockCopyOption = mock(CopyOption.class);
        assertEquals(mockPath2, Files.move(mockPath, mockPath2, mockCopyOption));
        verify(mockFileSystemProvider).move(mockPath, mockPath2, mockCopyOption);
    }

    @Test
    public void test_readSymbolicLink() throws IOException {
        when(mockFileSystemProvider.readSymbolicLink(mockPath)).thenReturn(mockPath2);
        assertEquals(mockPath2, Files.readSymbolicLink(mockPath));
        verify(mockFileSystemProvider).readSymbolicLink(mockPath);
    }

    @Test
    public void test_isSameFile() throws IOException {
        when(mockFileSystemProvider.isSameFile(mockPath, mockPath2)).thenReturn(true);
        when(mockFileSystemProvider.isSameFile(mockPath2, mockPath)).thenReturn(false);
        assertTrue(Files.isSameFile(mockPath, mockPath2));
        assertFalse(Files.isSameFile(mockPath2, mockPath));
    }

    @Test
    public void test_getFileStore() throws IOException {
        FileStore mockFileStore = mock(FileStore.class);
        when(mockFileSystemProvider.getFileStore(mockPath)).thenReturn(mockFileStore);
        assertEquals(mockFileStore, Files.getFileStore(mockPath));
    }

    @Test
    public void test_isHidden() throws IOException {
        when(mockFileSystemProvider.isHidden(mockPath)).thenReturn(true);
        when(mockFileSystemProvider.isHidden(mockPath2)).thenReturn(false);
        assertTrue(Files.isHidden(mockPath));
        assertFalse(Files.isHidden(mockPath2));
    }

    @Test
    public void test_probeContentType() throws IOException {
        assertEquals("text/plain",
                Files.probeContentType(filesSetup.getPathInTestDir("file.txt")));
        assertEquals("text/x-java",
                Files.probeContentType(filesSetup.getPathInTestDir("file.java")));
    }

    @Test
    public void test_getFileAttributeView() throws IOException {
        FileAttributeView mockFileAttributeView = mock(FileAttributeView.class);
        when(mockFileSystemProvider.getFileAttributeView(mockPath, FileAttributeView.class,
                LinkOption.NOFOLLOW_LINKS)).thenReturn(mockFileAttributeView);
        assertEquals(mockFileAttributeView, Files.getFileAttributeView(mockPath,
                FileAttributeView.class, LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void test_readAttributes() throws IOException {
        BasicFileAttributes mockBasicFileAttributes = mock(BasicFileAttributes.class);
        when(mockFileSystemProvider.readAttributes(mockPath, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS)).thenReturn(mockBasicFileAttributes);
        assertEquals(mockBasicFileAttributes, Files.readAttributes(mockPath,
                BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS));

    }

    @Test
    public void test_setAttribute() throws IOException {
        assertEquals(mockPath, Files.setAttribute(mockPath, "string", 10,
                LinkOption.NOFOLLOW_LINKS));
        verify(mockFileSystemProvider).setAttribute(mockPath, "string", 10,
                LinkOption.NOFOLLOW_LINKS);
    }

    @Test
    public void test_getAttribute() throws IOException {
        // Other tests are covered in test_readAttributes.
        // When file is NON_EXISTENT.
        try {
            Files.getAttribute(filesSetup.getTestPath(), "basic:lastModifiedTime");
            fail();
        } catch (NoSuchFileException expected) {}
    }

    @Test
    public void test_getAttribute_Exception() throws IOException {
        // IllegalArgumentException
        try {
            Files.getAttribute(filesSetup.getDataFilePath(), "xyz");
            fail();
        } catch (IllegalArgumentException expected) {}

        try {
            Files.getAttribute(null, "xyz");
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.getAttribute(filesSetup.getDataFilePath(), null);
            fail();
        } catch(NullPointerException expected) {}
    }

    @Test
    public void test_getPosixFilePermissions() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        Files.createFile(filesSetup.getTestPath(), attr);
        assertEquals(attr.value(), Files.getPosixFilePermissions(filesSetup.getTestPath()));
    }

    @Test
    public void test_getPosixFilePermissions_NPE() throws IOException {
        try {
            Files.getPosixFilePermissions(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_setPosixFilePermissions() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        Files.setPosixFilePermissions(filesSetup.getDataFilePath(), perm);
        assertEquals(attr.value(), Files.getPosixFilePermissions(filesSetup.getDataFilePath()));
    }

    @Test
    public void test_setPosixFilePermissions_NPE() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        try {
            Files.setPosixFilePermissions(null, perm);
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.setPosixFilePermissions(filesSetup.getDataFilePath(), null);
            fail();
        } catch(NullPointerException expected) {}
    }

    @Test
    public void test_getOwner() throws IOException, InterruptedException {
        String[] statCmd = { "stat", "-c", "%U", filesSetup.getTestDir() + "/" + DATA_FILE };
        Process statProcess = execCmdAndWaitForTermination(statCmd);
        String owner = readFromInputStream(statProcess.getInputStream()).trim();
        assertEquals(owner, Files.getOwner(filesSetup.getDataFilePath()).getName());
    }

    @Test
    public void test_getOwner_NPE() throws IOException, InterruptedException {
        try {
            Files.getOwner(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_setOwner() throws IOException {
        // TODO: unable to set the owner due to insufficient permissions.
    }

    @Test
    public void test_isSymbolicLink() throws IOException, InterruptedException {
        assertFalse(Files.isSymbolicLink(filesSetup.getTestPath()));
        assertFalse(Files.isSymbolicLink(filesSetup.getDataFilePath()));

        // Creating a symbolic link.
        String[] symLinkCmd = { "ln", "-s", DATA_FILE,
                filesSetup.getTestDir() + "/" + NON_EXISTENT_FILE };
        execCmdAndWaitForTermination(symLinkCmd);
        assertTrue(Files.isSymbolicLink(filesSetup.getTestPath()));
    }

    @Test
    public void test_isSymbolicLink_NPE() throws IOException, InterruptedException {
        try {
            Files.isSymbolicLink(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_isDirectory() throws IOException, InterruptedException {
        assertFalse(Files.isDirectory(filesSetup.getDataFilePath()));
        // When file doesn't exist.
        assertFalse(Files.isDirectory(filesSetup.getTestPath()));

        // Creating a directory.
        String dirName = "newDir";
        Path dirPath = filesSetup.getPathInTestDir(dirName);
        String mkdir[] = { "mkdir", filesSetup.getTestDir() + "/" + dirName };
        execCmdAndWaitForTermination(mkdir);
        assertTrue(Files.isDirectory(dirPath));
    }

    @Test
    public void test_isDirectory_NPE() throws IOException {
        try {
            Files.isDirectory(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_isRegularFile() throws IOException, InterruptedException {
        assertTrue(Files.isRegularFile(filesSetup.getDataFilePath()));
        // When file doesn't exist.
        assertFalse(Files.isRegularFile(filesSetup.getTestPath()));

        // Check directories.
        Path dirPath = filesSetup.getPathInTestDir("dir");
        Files.createDirectory(dirPath);
        assertFalse(Files.isRegularFile(dirPath));

        // Check symbolic link.
        // When linked to itself.
        Files.createSymbolicLink(filesSetup.getTestPath(),
                filesSetup.getTestPath().toAbsolutePath());
        assertFalse(Files.isRegularFile(filesSetup.getTestPath()));

        // When linked to some other file.
        filesSetup.reset();
        Files.createSymbolicLink(filesSetup.getTestPath(),
                filesSetup.getDataFilePath().toAbsolutePath());
        assertTrue(Files.isRegularFile(filesSetup.getTestPath()));

        // When asked to not follow the link.
        assertFalse(Files.isRegularFile(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));

        // Device file.
        Path deviceFilePath = Paths.get("/dev/null");
        assertTrue(Files.exists(deviceFilePath));
        assertFalse(Files.isRegularFile(deviceFilePath));
    }

    @Test
    public void test_isRegularFile_NPE() throws IOException {
        try {
            Files.isReadable(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_getLastModifiedTime() throws IOException, InterruptedException {
        String touchCmd[] = { "touch", "-d", "2015-10-09T00:00:00Z",
                filesSetup.getTestDir() + "/" + DATA_FILE };
        execCmdAndWaitForTermination(touchCmd);
        assertEquals("2015-10-09T00:00:00Z",
                Files.getLastModifiedTime(filesSetup.getDataFilePath()).toString());

        // Non existent file.
        try {
            Files.getLastModifiedTime(filesSetup.getTestPath()).toString();
            fail();
        } catch (NoSuchFileException expected) {}
    }

    @Test
    public void test_getLastModifiedTime_NPE() throws IOException {
        try {
            Files.getLastModifiedTime(null, LinkOption.NOFOLLOW_LINKS);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.getLastModifiedTime(filesSetup.getDataFilePath(), (LinkOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_setLastModifiedTime() throws IOException, InterruptedException {
        long timeInMillisToBeSet = System.currentTimeMillis() - 10000;
        Files.setLastModifiedTime(filesSetup.getDataFilePath(),
                FileTime.fromMillis(timeInMillisToBeSet));
        assertEquals(timeInMillisToBeSet/1000,
                Files.getLastModifiedTime(filesSetup.getDataFilePath()).to(TimeUnit.SECONDS));

        // Non existent file.
        try {
            Files.setLastModifiedTime(filesSetup.getTestPath(),
                    FileTime.fromMillis(timeInMillisToBeSet));
            fail();
        } catch (NoSuchFileException expected) {}
    }

    @Test
    public void test_setLastModifiedTime_NPE() throws IOException, InterruptedException {
        try {
            Files.setLastModifiedTime(null, FileTime.fromMillis(System.currentTimeMillis()));
            fail();
        } catch (NullPointerException expected) {}

        // No NullPointerException.
        Files.setLastModifiedTime(filesSetup.getDataFilePath(), null);
    }

    @Test
    public void test_size() throws IOException, InterruptedException {
        int testSizeInBytes = 5000;
        String ddCmd[] = { "dd", "if=/dev/zero", "of=" + filesSetup.getTestDir() + "/" + DATA_FILE,
                "bs="
                + testSizeInBytes, "count=1"};
        execCmdAndWaitForTermination(ddCmd);

        assertEquals(testSizeInBytes, Files.size(filesSetup.getDataFilePath()));

        try {
            Files.size(filesSetup.getTestPath());
            fail();
        } catch (NoSuchFileException expected) {}
    }

    @Test
    public void test_size_NPE() throws IOException, InterruptedException {
        try {
            Files.size(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_exists() throws IOException {
        // When file exists.
        assertTrue(Files.exists(filesSetup.getDataFilePath()));

        // When file doesn't exist.
        assertFalse(Files.exists(filesSetup.getTestPath()));

        // SymLink
        Files.createSymbolicLink(filesSetup.getTestPath(),
                filesSetup.getDataFilePath().toAbsolutePath());
        assertTrue(Files.exists(filesSetup.getTestPath()));

        // When link shouldn't be followed
        assertTrue(Files.exists(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));

        // When the target file doesn't exist.
        Files.delete(filesSetup.getDataFilePath());
        assertTrue(Files.exists(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));
        assertFalse(Files.exists(filesSetup.getTestPath()));

        // Symlink to itself
        filesSetup.reset();
        Files.createSymbolicLink(filesSetup.getTestPath(),
                filesSetup.getTestPath().toAbsolutePath());
        assertFalse(Files.exists(filesSetup.getTestPath()));
        assertTrue(Files.exists(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void test_exists_NPE() throws IOException {
        try {
            Files.exists(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_notExists() throws IOException {
        // When file exists.
        assertFalse(Files.notExists(filesSetup.getDataFilePath()));

        // When file doesn't exist.
        assertTrue(Files.notExists(filesSetup.getTestPath()));

        // SymLink
        Files.createSymbolicLink(filesSetup.getTestPath(),
                filesSetup.getDataFilePath().toAbsolutePath());
        assertFalse(Files.notExists(filesSetup.getTestPath()));

        // When link shouldn't be followed
        assertFalse(Files.notExists(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));

        // When the target file doesn't exist.
        Files.delete(filesSetup.getDataFilePath());
        assertFalse(Files.notExists(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));
        assertTrue(Files.notExists(filesSetup.getTestPath()));

        // Symlink to itself
        filesSetup.reset();
        Files.createSymbolicLink(filesSetup.getTestPath(),
                filesSetup.getTestPath().toAbsolutePath());
        assertFalse(Files.notExists(filesSetup.getTestPath()));
        assertFalse(Files.notExists(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void test_notExists_NPE() throws IOException {
        try {
            Files.notExists(null);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.notExists(filesSetup.getDataFilePath(), (LinkOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_isReadable() throws IOException {
        // When a readable file is available.
        assertTrue(Files.isReadable(filesSetup.getDataFilePath()));

        // When a file doesn't exist.
        assertFalse(Files.isReadable(filesSetup.getTestPath()));

        // Setting non readable permission for user
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("-wxrwxrwx");
        Files.setPosixFilePermissions(filesSetup.getDataFilePath(), perm);
        assertFalse(Files.isReadable(filesSetup.getDataFilePath()));
    }

    @Test
    public void test_isReadable_NPE() throws IOException {
        try {
            Files.isReadable(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_isWritable() throws IOException {
        // When a readable file is available.
        assertTrue(Files.isWritable(filesSetup.getDataFilePath()));

        // When a file doesn't exist.
        assertFalse(Files.isWritable(filesSetup.getTestPath()));

        // Setting non writable permission for user
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("r-xrwxrwx");
        Files.setPosixFilePermissions(filesSetup.getDataFilePath(), perm);
        assertFalse(Files.isWritable(filesSetup.getDataFilePath()));
    }

    @Test
    public void test_isWritable_NPE() {
        try {
            Files.isWritable(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_isExecutable() throws IOException {
        // When a readable file is available.
        assertFalse(Files.isExecutable(filesSetup.getDataFilePath()));

        // When a file doesn't exist.
        assertFalse(Files.isExecutable(filesSetup.getTestPath()));

        // Setting non executable permission for user
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rw-rwxrwx");
        Files.setPosixFilePermissions(filesSetup.getDataFilePath(), perm);
        assertFalse(Files.isExecutable(filesSetup.getDataFilePath()));
    }

    @Test
    public void test_isExecutable_NPE() {
        try {
            Files.isExecutable(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_walkFileTree$Path$Set$int$FileVisitor_symbolicLinkFollow()
            throws IOException, InterruptedException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   └── dir2 ─ dir3-file1 - file3
        //        │
        //        └── file2
        //
        // With follow link it should be able to traverse to dir3 and file1 when started from file2.

        // Directory setup.
        Path rootDir = filesSetup.getPathInTestDir("root");
        Path dir1 = filesSetup.getPathInTestDir("root/dir1");
        Path dir2 = filesSetup.getPathInTestDir("root/dir1/dir2");
        Path dir3 = filesSetup.getPathInTestDir("root/dir1/dir2/dir3");
        Path file1 = filesSetup.getPathInTestDir("root/dir1/dir2/dir3/file1");
        Path file2 = filesSetup.getPathInTestDir("root/file2");

        Files.createDirectories(dir3);
        Files.createFile(file1);
        Files.createSymbolicLink(file2, dir2.toAbsolutePath());
        assertTrue(Files.isSymbolicLink(file2));

        Map<Object, VisitOption> dirMap = new HashMap<>();
        Map<Object, VisitOption> expectedDirMap = new HashMap<>();
        Set<FileVisitOption> option = new HashSet<>();
        option.add(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(file2, option, 50, new TestFileVisitor(dirMap, option));

        expectedDirMap.put(file1.getFileName(), VisitOption.VISIT_FILE);
        expectedDirMap.put(file2.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(dir3.getFileName(), VisitOption.POST_VISIT_DIRECTORY);

        assertEquals(expectedDirMap, dirMap);
    }

    @Test
    public void test_walkFileTree$Path$FileVisitor() throws IOException {
        // Directory structure.
        //    .
        //    ├── DATA_FILE
        //    └── root
        //        ├── dir1
        //        │   ├── dir2
        //        │   │   ├── dir3
        //        │   │   └── file5
        //        │   ├── dir4
        //        │   └── file3
        //        ├── dir5
        //        └── file1
        //

        // Directory Setup.
        Path rootDir = filesSetup.getPathInTestDir("root");
        Path dir1 = filesSetup.getPathInTestDir("root/dir1");
        Path dir2 = filesSetup.getPathInTestDir("root/dir1/dir2");
        Path dir3 = filesSetup.getPathInTestDir("root/dir1/dir2/dir3");
        Path dir4 = filesSetup.getPathInTestDir("root/dir1/dir4");
        Path dir5 = filesSetup.getPathInTestDir("root/dir5");
        Path file1 = filesSetup.getPathInTestDir("root/file1");
        Path file3 = filesSetup.getPathInTestDir("root/dir1/file3");
        Path file5 = filesSetup.getPathInTestDir("root/dir1/dir2/file5");

        Files.createDirectories(dir3);
        Files.createDirectories(dir4);
        Files.createDirectories(dir5);
        Files.createFile(file3);
        Files.createFile(file5);
        Files.createSymbolicLink(file1, filesSetup.getDataFilePath().toAbsolutePath());

        Map<Object, VisitOption> dirMap = new HashMap<>();
        Map<Object, VisitOption> expectedDirMap = new HashMap<>();
        Path returnedPath = Files.walkFileTree(rootDir, new Files2Test.TestFileVisitor(dirMap));

        assertEquals(rootDir, returnedPath);

        expectedDirMap.put(rootDir.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(dir1.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(dir2.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(dir3.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(file5.getFileName(), VisitOption.VISIT_FILE);
        expectedDirMap.put(dir4.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(file3.getFileName(), VisitOption.VISIT_FILE);
        expectedDirMap.put(dir5.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(file1.getFileName(), VisitOption.VISIT_FILE);
        assertEquals(expectedDirMap, dirMap);
    }

    @Test
    public void test_walkFileTree_depthFirst() throws IOException {
        // Directory structure.
        //    .
        //    ├── DATA_FILE
        //    └── root
        //        ├── dir1 ── file1
        //        └── dir2 ── file2

        // Directory Setup.
        Path rootDir = filesSetup.getPathInTestDir("root");
        Path dir1 = filesSetup.getPathInTestDir("root/dir1");
        Path dir2 = filesSetup.getPathInTestDir("root/dir2");
        Path file1 = filesSetup.getPathInTestDir("root/dir1/file1");
        Path file2 = filesSetup.getPathInTestDir("root/dir2/file2");

        Files.createDirectories(dir1);
        Files.createDirectories(dir2);
        Files.createFile(file1);
        Files.createFile(file2);

        Map<Object, VisitOption> dirMap = new HashMap<>();
        List<Object> keyList = new ArrayList<>();
        Files.walkFileTree(rootDir,
                new Files2Test.TestFileVisitor(dirMap, keyList));
        assertEquals(rootDir.getFileName(), keyList.get(0));
        if (keyList.get(1).equals(dir1.getFileName())) {
            assertEquals(file1.getFileName(), keyList.get(2));
            assertEquals(dir2.getFileName(), keyList.get(3));
            assertEquals(file2.getFileName(), keyList.get(4));
        } else if (keyList.get(1).equals(dir2.getFileName())){
            assertEquals(file2.getFileName(), keyList.get(2));
            assertEquals(dir1.getFileName(), keyList.get(3));
            assertEquals(file1.getFileName(), keyList.get(4));
        } else {
            fail();
        }
    }

    @Test
    public void test_walkFileTree_negativeDepth() throws IOException {
        Path rootDir = filesSetup.getPathInTestDir("root");
        Path dir1 = filesSetup.getPathInTestDir("root/dir1");

        Files.createDirectories(dir1);

        Map<Object, VisitOption> dirMap = new HashMap<>();
        Set<FileVisitOption> option = new HashSet<>();
        option.add(FileVisitOption.FOLLOW_LINKS);
        try {
            Files.walkFileTree(rootDir, option, -1,
                    new Files2Test.TestFileVisitor(dirMap));
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void test_walkFileTree_maximumDepth() throws IOException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   ├── dir2
        //        │   │   ├── dir3
        //        │   │   └── file5
        //        │   ├── dir4
        //        │   └── file3
        //        ├── dir5
        //        └── file1
        //
        // depth will be 2. file5, dir3 is not reachable.
        // Directory Setup.
        Path rootDir = filesSetup.getPathInTestDir("root");
        Path dir1 = filesSetup.getPathInTestDir("root/dir1");
        Path dir2 = filesSetup.getPathInTestDir("root/dir1/dir2");
        Path dir3 = filesSetup.getPathInTestDir("root/dir1/dir2/dir3");
        Path dir4 = filesSetup.getPathInTestDir("root/dir1/dir4");
        Path dir5 = filesSetup.getPathInTestDir("root/dir5");
        Path file1 = filesSetup.getPathInTestDir("root/file1");
        Path file3 = filesSetup.getPathInTestDir("root/dir1/file3");
        Path file5 = filesSetup.getPathInTestDir("root/dir1/dir2/file5");

        Files.createDirectories(dir3);
        Files.createDirectories(dir4);
        Files.createDirectories(dir5);
        Files.createFile(file1);
        Files.createFile(file3);
        Files.createFile(file5);

        Map<Object, VisitOption> dirMap = new HashMap<>();
        Map<Object, VisitOption> expectedDirMap = new HashMap<>();
        Set<FileVisitOption> option = new HashSet<>();
        option.add(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(rootDir, option, 2, new Files2Test.TestFileVisitor(dirMap));
        assertTrue(Files.isDirectory(dir4));
        expectedDirMap.put(rootDir.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(dir1.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        // Both of the directories are at maximum depth, therefore, will be treated as simple file.
        expectedDirMap.put(dir2.getFileName(), VisitOption.VISIT_FILE);
        expectedDirMap.put(dir4.getFileName(), VisitOption.VISIT_FILE);
        expectedDirMap.put(dir5.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(file1.getFileName(), VisitOption.VISIT_FILE);
        expectedDirMap.put(file3.getFileName(), VisitOption.VISIT_FILE);

        assertEquals(expectedDirMap, dirMap);
    }

    @Test
    public void test_walkFileTree$Path$FileVisitor_NPE() throws IOException {
        Path rootDir = filesSetup.getPathInTestDir("root");
        try {
            Files.walkFileTree(null,
                    new Files2Test.TestFileVisitor(new HashMap<>()));
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.walkFileTree(rootDir, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_walkFileTree$Path$FileVisitor_FileSystemLoopException() throws IOException {
        // Directory structure.
        //    .
        //    ├── DATA_FILE
        //    └── root
        //        └── dir1
        //             └── file1
        //
        // file1 is symlink to dir1

        // Directory Setup.
        Path rootDir = filesSetup.getPathInTestDir("root");
        Path dir1 = filesSetup.getPathInTestDir("root/dir1");
        Path file1 = filesSetup.getPathInTestDir("root/dir1/file1");

        Files.createDirectories(dir1);
        Files.createSymbolicLink(file1, dir1.toAbsolutePath());
        assertEquals(dir1.getFileName(), Files.readSymbolicLink(file1).getFileName());

        Map<Object, VisitOption> dirMap = new HashMap<>();
        Set<FileVisitOption> option = new HashSet<>();
        option.add(FileVisitOption.FOLLOW_LINKS);
        try {
            Files.walkFileTree(rootDir, option, Integer.MAX_VALUE,
                    new Files2Test.TestFileVisitor(dirMap));
            fail();
        } catch (FileSystemLoopException expected) {}
    }

    // -- Mock Class --

    private static class TestFileVisitor implements FileVisitor<Path> {

        final Map<Object, VisitOption> dirMap;
        LinkOption option[];
        List<Object> keyList;

        public TestFileVisitor(Map<Object, VisitOption> dirMap) {
            this(dirMap, (List<Object>) null);
        }

        public TestFileVisitor(Map<Object, VisitOption> dirMap, Set<FileVisitOption> option) {
            this.dirMap = dirMap;
            for (FileVisitOption fileVisitOption : option) {
                if (fileVisitOption.equals(FileVisitOption.FOLLOW_LINKS)) {
                    this.option = new LinkOption[0];
                }
            }

            if (this.option == null) {
                this.option = new LinkOption[] {LinkOption.NOFOLLOW_LINKS};
            }
        }

        public TestFileVisitor(Map<Object, VisitOption> dirMap, List<Object> pathList) {
            this.dirMap = dirMap;
            this.option = new LinkOption[] {LinkOption.NOFOLLOW_LINKS};
            keyList = pathList;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
            if (keyList != null) {
                keyList.add(dir.getFileName());
            }
            dirMap.put(dir.getFileName(), VisitOption.PRE_VISIT_DIRECTORY);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (keyList != null) {
                keyList.add(file.getFileName());
            }
            dirMap.put(file.getFileName(), VisitOption.VISIT_FILE);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if (exc != null) {
                throw exc;
            }
            return TERMINATE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                throw exc;
            }
            if (dirMap.getOrDefault(dir.getFileName(), VisitOption.UNVISITED)
                    != VisitOption.PRE_VISIT_DIRECTORY) {
                return TERMINATE;
            } else {
                dirMap.put(dir.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
                return CONTINUE;
            }
        }
    }

    private enum VisitOption {
        PRE_VISIT_DIRECTORY,
        VISIT_FILE,
        POST_VISIT_DIRECTORY,
        UNVISITED,
    }
}
