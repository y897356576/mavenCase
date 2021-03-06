package com.stone.noteManage.service;

import com.stone.core.exception.MyException;
import com.stone.core.model.*;
import com.stone.noteManage.repository.NoteMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 石头 on 2018/2/24.
 */
@Service
public class NoteService {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private NotePersistProcessor persistProcessor;

    Logger logger = LoggerFactory.getLogger(NoteService.class);

    /**
     * 笔记文件导入
     * @param user
     * @param genreName
     * @param file
     * @return
     */
    public String noteImport(User user, String genreName, MultipartFile file) {
        if (file == null || file.getSize() <= 0) {
            throw new MyException("文件上传失败：上传的文件不存在。");
        }
        if (user == null) {
            throw new MyException("文件上传失败：当前用户不可用，请重新登录。");
        }

        Note note = new Note();
        NoteGenre noteGenre = note.getNoteGenre();
        NoteFile noteFile = note.getNoteFile();

        try {
            String originalName = file.getOriginalFilename();   //文件名称
            String fileType = originalName.substring(originalName.lastIndexOf(".") + 1);  //文件后缀

            note.setNoteName(originalName.substring(0, originalName.lastIndexOf(".")));
            note.setCreateUserId(user.getId());

            if ("txt|xml|conf|java|properties|js|css".contains(fileType)) {
                note.setContent(new String(file.getBytes()));
            }

            noteGenre.setTypeName(StringUtils.isBlank(genreName) ? "默认" : genreName.trim());

            noteFile.setFileName(note.getNoteName());
            noteFile.setFileContentType(file.getContentType());
            noteFile.setFileType(fileType);

            this.fileTransfer(user, file, noteFile);

            //数据入库
            persistProcessor.persistNote(note);

        } catch (Exception e) {
            logger.error("文件上传异常", e);
            throw new MyException("文件上传异常；" + e.getMessage());
        }
        return note.getId();
    }

    /**
     * 文件转储至服务器
     * @param user
     * @param file
     * @param noteFile
     * @throws IOException
     */
    private void fileTransfer(User user, MultipartFile file, NoteFile noteFile) throws IOException {
        String realPath = this.getWebPath();

        realPath += File.separator + "files" + File.separator + user.getId() + File.separator;
        String filePath = realPath + noteFile.getFileName() + "." + noteFile.getFileType();
        File realPathFile = new File(realPath);
        File filePathFile = new File(filePath);

        if (!realPathFile.exists()) {
            realPathFile.mkdirs();
        }
        if (!filePathFile.exists()) {
            file.transferTo(filePathFile);
        }
        noteFile.setFilePath(filePath);
    }

    /**
     * 获取笔记列表
     * @param user
     * @return
     */
    public List<Note> noteList(User user) {
        return noteMapper.getNotesByUserId(user.getId());
    }

    /**
     * 获取笔记详情
     * @param user
     * @param noteId
     * @return
     */
    public Note noteDetail(HttpServletRequest request, User user, String noteId) {
        Note note = noteMapper.getNoteById(user.getId(), noteId);
        if (note == null) {
            return note;
        }
        if (!"jpg|jpeg|png|bmp|gif".contains(note.getNoteFile().getFileType())) {
            this.composeContent(note);
        }
        return note;
    }

    /**
     * 加载图片文件
     * @param response
     * @param filePath
     */
    public void noteDetailImg(HttpServletResponse response, String filePath) {
        try {
            InputStream is = new FileInputStream(new File(filePath));
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            response.setContentType("image/jpeg");
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            logger.error("图片加载失败", e);
            throw new MyException("图片加载失败");
        }
    }

    /**
     * 笔记顺序调整
     * @param user
     * @param noteId
     * @return
     */
    public void noteIndexModify(User user, String noteId, Integer indexModify) {
        List<Note> notes = noteMapper.getNotesByUserIdAndGenre(user.getId(), noteId);
        if (CollectionUtils.isEmpty(notes)) {
            return;
        }
        List<Note> updates = new ArrayList<>();
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getId().equals(noteId)) {
                if (indexModify > 0) {
                    if (i == notes.size() - 1) {
                        return;
                    } else {
                        Integer index = notes.get(i + 1).getNoteIndex();
                        notes.get(i + 1).setNoteIndex(notes.get(i).getNoteIndex());
                        notes.get(i).setNoteIndex(index);
                        updates.add(notes.get(i));
                        updates.add(notes.get(i + 1));
                    }
                } else {
                    if (i == 0) {
                        return;
                    } else {
                        Integer index = notes.get(i - 1).getNoteIndex();
                        notes.get(i - 1).setNoteIndex(notes.get(i).getNoteIndex());
                        notes.get(i).setNoteIndex(index);
                        updates.add(notes.get(i));
                        updates.add(notes.get(i - 1));
                    }
                }
            }
        }

        noteMapper.noteIndexModify(updates);
    }

    /**
     * 笔记删除
     * @param user
     * @param noteId
     * @return
     */
    public void noteDelete(User user, String noteId) {
        noteMapper.noteDelete(user.getId(), noteId);
    }

    /**
     * 根据Note的NoteContent列表合成Content字段
     * @param note
     */
    private void composeContent(Note note) {
        StringBuilder content = new StringBuilder();
        if (CollectionUtils.isNotEmpty(note.getContents())) {
            for (NoteContent temp : note.getContents()) {
                if (StringUtils.isNotBlank(temp.getContent())) {
                    content.append(temp.getContent());
                }
            }
        }
        note.setContent(content.toString());
    }

    /**
     * 获取项目所在路径
     * @return
     */
    private String getWebPath() {
        String realPath = getClass().getResource("/").getFile().toString();
        realPath = realPath.substring(0, realPath.lastIndexOf("/"));
        realPath = realPath.substring(0, realPath.lastIndexOf("/"));
        realPath = realPath.substring(0, realPath.lastIndexOf("/"));
        return realPath;
    }

}
