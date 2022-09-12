package com.example.filesystemservice.controller;

import com.example.filesystemservice.dto.NodeDto;
import com.example.filesystemservice.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path = "/")
public class MainController {
    @Autowired
    NodeService nodeService;

    @PostMapping(path = "imports")
    @ResponseBody
    public String imports(@RequestBody String batch) {
        nodeService.importBatch(batch);
        return "";
    }

    @DeleteMapping(path = "delete")
    @ResponseBody
    public String delete(@PathVariable String id) {
        nodeService.deleteNodeById(id);
        return "";
    }

    @GetMapping(path = "nodes/{id}")
    @ResponseBody
    public NodeDto nodes(@PathVariable String id) {
        return nodeService.getNodeById(id);
    }

}
