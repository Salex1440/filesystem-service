package com.example.filesystemservice.controller;

import com.example.filesystemservice.dto.HistoryDto;
import com.example.filesystemservice.dto.NodeDto;
import com.example.filesystemservice.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @DeleteMapping(path = "delete/{id}")
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

    @GetMapping(path = "updates")
    @ResponseBody
    public List<NodeDto> updates(@RequestParam String date){
        return nodeService.findUpdatedNodes(date);
    }

    @GetMapping(path = "node/{id}/history")
    @ResponseBody
    public HistoryDto getHistoryNode(@PathVariable String id,
                                     @RequestParam String dateStart,
                                     @RequestParam String dateEnd) {
        return nodeService.getHistory(id, dateStart, dateEnd);
    }

}
