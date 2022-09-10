package com.example.filesystemservice.service;

import com.example.filesystemservice.dto.BatchDto;
import com.example.filesystemservice.dto.ItemDto;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void importNode() {
        String filename = "json/batch.json";
        BatchDto batch = createBatchData(filename);
        doReturn(null).when(nodeRepositoryMock).save(any(Node.class));

        nodeService.importNode(batch);
        verify(nodeRepositoryMock).save(nodeCaptor.capture());

        ItemDto item = batch.getItems().get(0);
        String updateDate = batch.getUpdateDate();
        assertEquals(item.getId(), nodeCaptor.getValue().getId());
        assertEquals(item.getType(), nodeCaptor.getValue().getType());
        assertEquals(item.getUrl(), nodeCaptor.getValue().getUrl());
        assertEquals(updateDate, nodeCaptor.getValue().getDate());
        assertEquals(item.getSize(), nodeCaptor.getValue().getSize());
        assertEquals(item.getParentId(), nodeCaptor.getValue().getParentId());
        verify(nodeRepositoryMock, times(1)).save(any(Node.class));
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
}