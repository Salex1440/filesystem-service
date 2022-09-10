package com.example.filesystemservice.service;

import com.example.filesystemservice.dto.BatchDto;
import com.example.filesystemservice.repository.Node;
import com.example.filesystemservice.repository.NodeRepository;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NodeServiceTest {

    @Mock
    NodeRepository nodeRepositoryMock;

    @InjectMocks
    NodeService nodeService;

    AutoCloseable closeable;

    @Test
    void importNode() {
        String filename = "json/batch.json";
        BatchDto batch = createBatchData(filename);
        doReturn(null).when(nodeRepositoryMock).save(any(Node.class));
        nodeService.importNode(batch);
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