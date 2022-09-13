package com.example.filesystemservice.service;

import com.example.filesystemservice.dto.BatchDto;
import com.example.filesystemservice.dto.ItemDto;
import com.example.filesystemservice.dto.NodeDto;
import com.example.filesystemservice.exception.BadRequestException;
import com.example.filesystemservice.exception.NotFoundException;
import com.example.filesystemservice.repository.Node;
import com.example.filesystemservice.repository.NodeRepository;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NodeServiceTest {

    @Mock
    NodeRepository nodeRepositoryMock;

    @InjectMocks
    NodeService nodeService;

    @Captor
    private ArgumentCaptor<Node> nodeCaptor;

    AutoCloseable closeable;

    @Test
    void importFolder() {
        String filename = "json/batchFolder.json";
        BatchDto batch = createBatchData(filename);
        doReturn(null).when(nodeRepositoryMock).save(any(Node.class));
        String json = createJsonData(filename);
        nodeService.importBatch(json);
        verify(nodeRepositoryMock).save(nodeCaptor.capture());

        ItemDto item = batch.getItems().get(0);
        String updateDate = batch.getUpdateDate();
        assertEquals(item.getId(), nodeCaptor.getValue().getId());
        assertEquals(item.getType(), nodeCaptor.getValue().getType());
        assertEquals(item.getUrl(), nodeCaptor.getValue().getUrl());
        Date date = nodeCaptor.getValue().getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String dateStr = dateFormat.format(date);
        assertEquals(updateDate, dateStr);

        assertEquals(item.getSize(), nodeCaptor.getValue().getSize());
        assertEquals(item.getParentId(), nodeCaptor.getValue().getParentId());
        verify(nodeRepositoryMock, times(1)).save(any(Node.class));
    }

    @Test
    void importFile() {
        String filename = "json/batchFile.json";
        BatchDto batch = createBatchData(filename);
        doReturn(null).when(nodeRepositoryMock).save(any(Node.class));
        String json = createJsonData(filename);
        nodeService.importBatch(json);
        verify(nodeRepositoryMock).save(nodeCaptor.capture());

        ItemDto item = batch.getItems().get(0);
        String updateDate = batch.getUpdateDate();
        assertEquals(item.getId(), nodeCaptor.getValue().getId());
        assertEquals(item.getType(), nodeCaptor.getValue().getType());
        assertEquals(item.getUrl(), nodeCaptor.getValue().getUrl());
        Date date = nodeCaptor.getValue().getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String dateStr = dateFormat.format(date);
        assertEquals(updateDate, dateStr);
        assertEquals(item.getSize(), nodeCaptor.getValue().getSize());
        assertEquals(item.getParentId(), nodeCaptor.getValue().getParentId());
        verify(nodeRepositoryMock, times(1)).save(any(Node.class));
    }

    @Test
    void badParent() throws ParseException {
        String file1 = "json/batchFile.json";
        String file2 = "json/BadParent.json";
        BatchDto batchFile1 = createBatchData(file1);
        ItemDto item = batchFile1.getItems().get(0);
        Node node = new Node();
        node.setId(item.getId());
        node.setType(item.getType());
        node.setUrl(item.getUrl());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = dateFormat.parse(batchFile1.getUpdateDate());
        node.setDate(date);
        node.setSize(item.getSize());
        node.setParentId(item.getParentId());
        BatchDto batchFile2 = createBatchData(file2);
        String json = createJsonData(file2);
        doReturn(null).when(nodeRepositoryMock).save(any(Node.class));
        doReturn(node).when(nodeRepositoryMock).findNodeById(batchFile2.getItems().get(0).getParentId());
        assertThrows(BadRequestException.class,
                () -> nodeService.importBatch(json),
                "Expected save() to throw a UnprocessableEntityException, but it didn't");
    }

    @Test
    void badFileSize() {
        String file = "json/BadFileSize.json";
        String json = createJsonData(file);
        doReturn(null).when(nodeRepositoryMock).save(any(Node.class));
        assertThrows(BadRequestException.class,
                () -> nodeService.importBatch(json),
                "Expected save() to throw a BadRequestException, but it didn't");
    }

    @Test
    void badFolderSize() {
        String file = "json/BadFolderSize.json";
        String json = createJsonData(file);
        doReturn(null).when(nodeRepositoryMock).save(any(Node.class));
        assertThrows(BadRequestException.class,
                () -> nodeService.importBatch(json),
                "Expected save() to throw a BadRequestException, but it didn't");
    }

    @Test
    void badFileUrl() {
        String file = "json/BadFileUrl.json";
        String json = createJsonData(file);
        doReturn(null).when(nodeRepositoryMock).save(any(Node.class));
        assertThrows(BadRequestException.class,
                () -> nodeService.importBatch(json),
                "Expected save() to throw a BadRequestException, but it didn't");
    }

    @Test
    void badFolderUrl() {
        String file = "json/BadFolderUrl.json";
        String json = createJsonData(file);
        doReturn(null).when(nodeRepositoryMock).save(any(Node.class));
        assertThrows(BadRequestException.class,
                () -> nodeService.importBatch(json),
                "Expected save() to throw a BadRequestException, but it didn't");
    }

    @Test
    void sameId() {
        String file = "json/SameId.json";
        String json = createJsonData(file);
        doReturn(null).when(nodeRepositoryMock).save(any(Node.class));
        assertThrows(BadRequestException.class,
                () -> nodeService.importBatch(json),
                "Expected save() to throw a BadRequestException, but it didn't");
    }

    @Test
    void badDate() {
        String file = "json/BadDate.json";
        String json = createJsonData(file);
        doReturn(null).when(nodeRepositoryMock).save(any(Node.class));
        assertThrows(BadRequestException.class,
                () -> nodeService.importBatch(json),
                "Expected save() to throw a BadRequestException, but it didn't");
    }

    @Test
    void getFileNodeById() throws ParseException {
        String filename = "json/batchFile.json";
        BatchDto batch = createBatchData(filename);
        ItemDto item = batch.getItems().get(0);
        Node node = new Node();
        node.setId(item.getId());
        node.setType(item.getType());
        node.setUrl(item.getUrl());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = dateFormat.parse(batch.getUpdateDate());
        node.setDate(date);
        node.setSize(item.getSize());
        node.setParentId(item.getParentId());
        doReturn(node).when(nodeRepositoryMock).findNodeById(node.getId());
        doReturn(null).when(nodeRepositoryMock).findNodesByParentId(anyString());

        NodeDto nodeDto = nodeService.getNodeById(node.getId());
        assertEquals(nodeDto.getId(), node.getId());
        assertEquals(nodeDto.getType(), node.getType());
        assertEquals(nodeDto.getUrl(), node.getUrl());
        assertEquals(nodeDto.getParentId(), node.getParentId());
        assertEquals(nodeDto.getSize(), node.getSize());
        assertNull(nodeDto.getChildren());
        String dateStr = dateFormat.format(node.getDate());
        assertEquals(nodeDto.getDate(), dateStr);
    }

    @Test
    void getFolderNodeById() throws ParseException {
        String filename = "json/batchFolder.json";
        BatchDto batch = createBatchData(filename);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = dateFormat.parse(batch.getUpdateDate());
        ItemDto item = batch.getItems().get(0);
        Node node = new Node();
        node.setId(item.getId());
        node.setType(item.getType());
        node.setUrl(item.getUrl());
        node.setDate(date);
        node.setSize(item.getSize());
        node.setParentId(item.getParentId());
        doReturn(node).when(nodeRepositoryMock).findNodeById(node.getId());
        doReturn(null).when(nodeRepositoryMock).findNodesByParentId(anyString());

        NodeDto nodeDto = nodeService.getNodeById(node.getId());
        assertEquals(nodeDto.getId(), node.getId());
        assertEquals(nodeDto.getType(), node.getType());
        assertEquals(nodeDto.getUrl(), node.getUrl());
        assertEquals(nodeDto.getParentId(), node.getParentId());
        assertEquals(nodeDto.getSize(), node.getSize());
        assertEquals(nodeDto.getChildren(), new ArrayList<>());
        String dateStr = dateFormat.format(node.getDate());
        assertEquals(nodeDto.getDate(), dateStr);
    }

    @Test
    void checkFolderSize() throws ParseException {
        String filename = "json/FolderSize.json";
        BatchDto batch = createBatchData(filename);
        List<Node> nodes = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = dateFormat.parse(batch.getUpdateDate());
        for (ItemDto item : batch.getItems()) {
            Node node = new Node();
            node.setId(item.getId());
            node.setType(item.getType());
            node.setUrl(item.getUrl());
            node.setDate(date);
            node.setSize(item.getSize());
            node.setParentId(item.getParentId());
            nodes.add(node);
            doReturn(node).when(nodeRepositoryMock).findNodeById(node.getId());
        }
        List<Node> children1 = Arrays.asList(nodes.get(1), nodes.get(2));
        doReturn(children1).when(nodeRepositoryMock).findNodesByParentId(nodes.get(0).getId());
        List<Node> children2 = Arrays.asList(nodes.get(3), nodes.get(4));
        doReturn(children2).when(nodeRepositoryMock).findNodesByParentId(nodes.get(1).getId());

        NodeDto result = nodeService.getNodeById("folderId1");
        assertEquals(result.getSize(), nodes.get(2).getSize() + nodes.get(3).getSize() + nodes.get(4).getSize());
        result = nodeService.getNodeById("folderId2");
        assertEquals(result.getSize(), nodes.get(3).getSize() + nodes.get(4).getSize());
    }

    @Test
    void getItemNotFound() {
        assertThrows(NotFoundException.class,
                () -> nodeService.getNodeById("notExistingId"),
                "Expected getNodeById() to throw a NotFoundException, but it didn't");
    }

    @Test
    void deleteNodeById() throws ParseException {
        String filename = "json/FolderSize.json";
        BatchDto batch = createBatchData(filename);
        List<Node> nodes = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = dateFormat.parse(batch.getUpdateDate());
        for (ItemDto item : batch.getItems()) {
            Node node = new Node();
            node.setId(item.getId());
            node.setType(item.getType());
            node.setUrl(item.getUrl());
            node.setDate(date);
            node.setSize(item.getSize());
            node.setParentId(item.getParentId());
            nodes.add(node);
            doReturn(node).when(nodeRepositoryMock).findNodeById(node.getId());
        }
        List<Node> children1 = Arrays.asList(nodes.get(1), nodes.get(2));
        doReturn(children1).when(nodeRepositoryMock).findNodesByParentId(nodes.get(0).getId());
        List<Node> children2 = Arrays.asList(nodes.get(3), nodes.get(4));
        doReturn(children2).when(nodeRepositoryMock).findNodesByParentId(nodes.get(1).getId());

        nodeService.deleteNodeById("fileId3");
        children2 = List.of(nodes.get(3));
        doReturn(children2).when(nodeRepositoryMock).findNodesByParentId(nodes.get(1).getId());
        nodeService.deleteNodeById("folderId2");
        children1 = List.of(nodes.get(2));
        doReturn(children1).when(nodeRepositoryMock).findNodesByParentId(nodes.get(0).getId());
        nodeService.deleteNodeById("folderId1");
    }

    @Test
    void deleteNodeNotFound() {
        assertThrows(NotFoundException.class,
                () -> nodeService.deleteNodeById("notExistingId"),
                "Expected getNodeById() to throw a NotFoundException, but it didn't");
    }


    @BeforeEach
    void initMocks() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }

    private BatchDto createBatchData(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        String content = "";
        try (InputStream inputStream = classLoader.getResourceAsStream(filename)) {
            assert inputStream != null;
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            content = json.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        return gson.fromJson(content, BatchDto.class);
    }

    private String createJsonData(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        String content = "";
        try (InputStream inputStream = classLoader.getResourceAsStream(filename)) {
            assert inputStream != null;
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            content = json.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

}