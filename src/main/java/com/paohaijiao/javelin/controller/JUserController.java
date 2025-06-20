/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright (c) [2025-2099] Martin (goudingcheng@gmail.com)
 */
package com.paohaijiao.javelin.controller;
import com.paohaijiao.javelin.controller.req.JFileUploadReq;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.paohaijiao.javelin.model.JUserModel;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/users")
public class JUserController {
    private static final String UPLOAD_DIR = "d://test//uploads/";

    private static List<JUserModel> list=new ArrayList<>();

    public JUserController() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            list.add(getMockData());
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }
    }
    private JUserModel getMockData(){
        return new JUserModel(1L, "Jojo", "github.com");
    }




    @GetMapping("/all")
    public ResponseEntity<List<JUserModel>> getAllUsers() {
        List<JUserModel> list= Arrays.asList(getMockData());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<JUserModel> getUserById(@PathVariable Long id) {
        JUserModel user = getMockData();
        if (user.getId().equals(id)) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<JUserModel> createUser(@RequestBody JUserModel user) {
        list.add(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JUserModel> updateUser(@PathVariable Long id, @RequestBody JUserModel user) {
        JUserModel user1 = getMockData();
        if (user1.getId().equals(id)) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JUserModel> patchUser(@PathVariable Long id, @RequestBody JUserModel partialUser) {
        JUserModel user1 = getMockData();
        if (user1.getId().equals(id)) {
            return new ResponseEntity<>(user1, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        JUserModel user1 = getMockData();
        if (user1.getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkUserExists(@PathVariable Long id) {
        JUserModel user1 = getMockData();
        if (user1.getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> options() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Allow", "GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS");
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/trace", method = RequestMethod.TRACE)
    public ResponseEntity<String> trace(@RequestBody(required = false) String body) {
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择文件上传");
        }
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
            Files.write(path, bytes);
            return ResponseEntity.ok("文件上传成功: " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文件上传失败: " + e.getMessage());
        }
    }
    @PostMapping("/upload-multiple")
    public ResponseEntity<String> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("请选择至少一个文件上传");
        }
        StringBuilder result = new StringBuilder();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    byte[] bytes = file.getBytes();
                    Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
                    Files.write(path, bytes);
                    result.append("文件 ").append(file.getOriginalFilename()).append(" 上传成功\n");
                } catch (IOException e) {
                    result.append("文件 ").append(file.getOriginalFilename())
                            .append(" 上传失败: ").append(e.getMessage()).append("\n");
                }
            }
        }
        return ResponseEntity.ok(result.toString());
    }
    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/upload-with-params")
    public ResponseEntity<String> uploadFileWithParams(
            @RequestParam("userId") String userId,
            @RequestParam("username") String username,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择文件上传");
        }
        try {
            System.out.println("接收到的参数 - userId: " + userId + ", username: " + username);
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            Path targetLocation = Paths.get(UPLOAD_DIR).resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);
            return ResponseEntity.ok(String.format(
                    "文件上传成功! 参数: userId=%s, username=%s, 文件名: %s, 大小: %d bytes",
                    userId, username, fileName, file.getSize()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("处理失败: " + e.getMessage());
        }
    }
    @PostMapping("/upload-with-dto")
    public ResponseEntity<String> uploadFileWithDTO(
            @RequestPart("data") JFileUploadReq data,
            @RequestPart("file") MultipartFile file) {
        return uploadFileWithParams(data.getUserId(), data.getUsername(), file);
    }

}
