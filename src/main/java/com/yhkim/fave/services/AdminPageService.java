package com.yhkim.fave.services;

import com.yhkim.fave.entities.*;
import com.yhkim.fave.mappers.*;
import com.yhkim.fave.vos.BoardPostPageVo;
import com.yhkim.fave.vos.IndexPageVo;
import com.yhkim.fave.vos.ReportsPageVo;
import com.yhkim.fave.vos.UserPageVo;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminPageService {
    private final WriteMapper writeMapper;
    private final BoardPostMapper boardPostsMapper;
    private final UserMapper userMapper;
    private final ReportsMapper reportsMapper;
    private final FaveInfoMapper faveInfoMapper;
    private final InquiriesArticleMapper inquiriesArticleMapper;


    @Autowired
    public AdminPageService(WriteMapper writeMapper, BoardPostMapper boardPostsMapper, UserMapper userMapper, ReportsMapper reportsMapper, FaveInfoMapper faveInfoMapper, InquiriesArticleMapper inquiriesArticleMapper) {
        this.writeMapper = writeMapper;
        this.boardPostsMapper = boardPostsMapper;
        this.userMapper = userMapper;
        this.reportsMapper = reportsMapper;
        this.faveInfoMapper = faveInfoMapper;
        this.inquiriesArticleMapper = inquiriesArticleMapper;
    }

    public Pair<IndexPageVo, UserEntity[]> selectIndexUser(int page) {
        page = Math.max(page, 1);
        int totalCount = this.userMapper.selectUserCount();
        IndexPageVo index = new IndexPageVo(page, totalCount);
        UserEntity[] user = this.userMapper.selectUserPage(index.countPerPage, index.offsetCount);
        return Pair.of(index, user);
    }

    public Pair<IndexPageVo, BoardPostEntity[]> selectIndexBoard(int page) {
        page = Math.max(page, 1);
        int totalCount = this.boardPostsMapper.selectBoardPostCount();
        IndexPageVo index = new IndexPageVo(page, totalCount);
        BoardPostEntity[] boardPosts = this.boardPostsMapper.selectBoardPost(index.countPerPage, index.offsetCount);

        for (BoardPostEntity boardPost : boardPosts) {
            UserEntity user = this.findUserByEmail(boardPost.getUserEmail());
            boardPost.setUserEmail(user.getEmail());
        }
        return Pair.of(index, boardPosts);
    }

    public Pair<IndexPageVo, InquiriesEntity[]> selectAllInquiries(int page) {
        page = Math.max(page, 1);
        int totalCount = this.inquiriesArticleMapper.selectInquiriesCount();
        IndexPageVo index = new IndexPageVo(page, totalCount);
        InquiriesEntity[] inquiries = this.inquiriesArticleMapper.selectInquiries(index.countPerPage, index.offsetCount);
        return Pair.of(index, inquiries);
    }

    public Pair<IndexPageVo, ReportEntity[]> selectIndexReport(int page) {
        page = Math.max(page, 1);
        int totalCount = this.reportsMapper.selectReportsCount();
        IndexPageVo index = new IndexPageVo(page, totalCount);
        ReportEntity[] reportEntities = this.reportsMapper.selectReports(index.countPerPage, index.offsetCount);
        return Pair.of(index, reportEntities);
    }

    public Boolean write(FaveInfoEntity adminPage, MultipartFile coverFile) {
        if (adminPage == null || adminPage.getTitle() == null || adminPage.getTitle().length() < 2 || adminPage.getTitle().length() > 20 ||
                adminPage.getLocation() == null || adminPage.getStartDate() == null || adminPage.getEndDate() == null || adminPage.getDescription() == null || adminPage.getDescription().isEmpty() || adminPage.getDescription().length() > 10000) {
            return false;
        }

        try {
            if (coverFile != null && !coverFile.isEmpty()) {
                adminPage.setCoverData(coverFile.getBytes());
                adminPage.setCoverContentType(coverFile.getContentType());
            }
            adminPage.setCreatedAt(LocalDateTime.now());
            adminPage.setUpdatedAt(null);

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail;
            if (principal instanceof UserDetails) {
                userEmail = ((UserEntity) principal).getEmail();
            } else {
                userEmail = principal.toString();
            }

            adminPage.setUserEmail(userEmail);

            return this.writeMapper.insertAdminWrite(adminPage) > 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public Boolean modify(FaveInfoEntity adminPage, MultipartFile coverFile, Boolean deleteCover) {
        if (adminPage == null) {
            return false;
        }

        FaveInfoEntity dbFaveInfo = this.faveInfoMapper.selectFaveInfoById(adminPage.getIndex());
        if (dbFaveInfo == null) {
            return false;
        }

        dbFaveInfo.setTitle(adminPage.getTitle());
        dbFaveInfo.setDescription(adminPage.getDescription());
        dbFaveInfo.setStartDate(adminPage.getStartDate());
        dbFaveInfo.setEndDate(adminPage.getEndDate());
        dbFaveInfo.setLocation(adminPage.getLocation());
        dbFaveInfo.setUpdatedAt(LocalDateTime.now());

        try {
            if (Boolean.TRUE.equals(deleteCover)) {
                dbFaveInfo.setCoverData(null);
                dbFaveInfo.setCoverContentType(null);
            } else if (coverFile != null && !coverFile.isEmpty()) {
                dbFaveInfo.setCoverData(coverFile.getBytes());
                dbFaveInfo.setCoverContentType(coverFile.getContentType());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return this.faveInfoMapper.updateFaveInfo(dbFaveInfo) > 0;
    }

    public boolean updateDeleted(String userEmail) {
        UserEntity user = this.userMapper.selectUserByEmailAdmin(userEmail);
        if (user == null) {
            return false;
        }
        user.setDeletedAt(LocalDateTime.now());
        user.setSuspended(true);
        return this.userMapper.updateWarning(user) > 0;
    }

    public boolean updateWarning(String userEmail, int warning) {
        UserEntity user = this.userMapper.selectUserByEmailAdmin(userEmail);
        System.out.println("user: " + user);
        if (user == null) {
            return false;
        }
        if (user.getWarning() < 3) {
            user.setWarning(warning + 1);
        }
        return this.userMapper.updateWarning(user) > 0;
    }

    public Pair<UserPageVo, UserEntity[]> selectUserPage(int page) {
        page = Math.max(page, 1);
        int totalCount = this.userMapper.selectUserCount();
        UserPageVo userPageVo = new UserPageVo(page, totalCount);
        UserEntity[] user = this.userMapper.selectUserPage(userPageVo.countPerPage, userPageVo.offsetCount);
        return Pair.of(userPageVo, user);
    }

    public Pair<UserPageVo, UserEntity[]> selectUserPageBySearch(String filter, String keyword, int page) {
        page = Math.max(page, 1);
        if (filter == null || (!filter.equals("email") && !filter.equals("nickname") && !filter.equals("contact") && !filter.equals("verified"))) {
            filter = "email";
        }
        if (keyword == null || keyword.isEmpty()) {
            keyword = "";
        }
        if (filter.equals("verified")) {
            if (keyword.equals("완료")) {
                keyword = "1";
            }
            if (keyword.equals("미완료")) {
                keyword = "";
            }
        }
        int totalCount = this.userMapper.selectUserCountBySearch(filter, keyword);
        UserPageVo userPageVo = new UserPageVo(page, totalCount);
        UserEntity[] user = this.userMapper.selectUserBySearch(filter, keyword, userPageVo.countPerPage, userPageVo.offsetCount);
        return Pair.of(userPageVo, user);
    }

    public Pair<BoardPostPageVo, BoardPostEntity[]> selectBoardPost(int page) {
        page = Math.max(page, 1);
        int totalCount = this.boardPostsMapper.selectBoardPostCount();
        BoardPostPageVo boardPostPageVo = new BoardPostPageVo(page, totalCount);
        BoardPostEntity[] boardPosts = this.boardPostsMapper.selectBoardPost(boardPostPageVo.countPerPage, boardPostPageVo.offsetCount);

        for (BoardPostEntity boardPost : boardPosts) {
            UserEntity user = this.findUserByEmail(boardPost.getUserEmail());
            boardPost.setUserEmail(user.getEmail());
        }
        return Pair.of(boardPostPageVo, boardPosts);
    }

    public Pair<BoardPostPageVo, BoardPostEntity[]> selectBoardPostBySearch(String filter, String keyword, int page) {
        page = Math.max(page, 1);
        if (filter == null || (!filter.equals("all") && !filter.equals("title") && !filter.equals("content") && !filter.equals("nickname"))) {
            filter = "all";
        }
        if (keyword == null || keyword.isEmpty()) {
            keyword = "";
        }
        int totalCount = this.boardPostsMapper.selectBoardPostCountBySearch(filter, keyword);
        BoardPostPageVo boardPostPageVo = new BoardPostPageVo(page, totalCount);
        BoardPostEntity[] boardPosts = this.boardPostsMapper.selectBoardPostBySearch(filter, keyword, boardPostPageVo.countPerPage, boardPostPageVo.offsetCount);

        for (BoardPostEntity boardPost : boardPosts) {
            UserEntity user = this.findUserByEmail(boardPost.getUserEmail());
            boardPost.setUserEmail(user.getEmail());
        }
        return Pair.of(boardPostPageVo, boardPosts);
    }

    public UserEntity findUserByEmail(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) {
            return null;
        }
        return this.userMapper.selectUserByEmailAdmin(userEmail);
        
    }

    public boolean deleteBoardPost(int index) {
        BoardPostEntity board = this.boardPostsMapper.selectBoardPostsByIndex(index);
        if (board == null) {
             return false;
        }
        board.setDeletedAt(LocalDateTime.now());
        return this.boardPostsMapper.updateBoardPost(board) > 0;
    }

    public Pair<ReportsPageVo, ReportEntity[]> selectReports(int page) {
        page = Math.max(page, 1);

        int totalCount = this.reportsMapper.selectReportsCount();
        ReportsPageVo reportsPageVo = new ReportsPageVo(page, totalCount);
        ReportEntity[] reportEntities = this.reportsMapper.selectReports(reportsPageVo.countPerPage, reportsPageVo.offsetCount);
        for (ReportEntity reportEntity : reportEntities) {
            UserEntity user = this.findUserByEmail(reportEntity.getReportedUserEmail());
            reportEntity.setUser(user);
        }
        return Pair.of(reportsPageVo, reportEntities);
    }

    public Boolean updateReport(int index) {
        ReportEntity reports = this.reportsMapper.selectReportByIndex(index);
        if (reports == null) {
            return false;
        }
        reports.setCurrentStatus("신고 처리 완료");
        return this.reportsMapper.updateReport(reports) > 0;
    }

    public Pair<UserPageVo, FaveInfoEntity[]> selectFaveInfo(int page) {
        page = Math.max(page, 1);
        int totalCount = this.faveInfoMapper.selectFaveInfoCount();
        UserPageVo userPageVo = new UserPageVo(page, totalCount);
        FaveInfoEntity[] fave = this.faveInfoMapper.selectFaveInfo(userPageVo.countPerPage, userPageVo.offsetCount);
        return Pair.of(userPageVo, fave);
    }

    public Map<String, String> splitAddress(String fullAddress) {
        Map<String, String> addressParts = new HashMap<>();

        String extraAddress = "";
        if (fullAddress.contains("(") && fullAddress.contains(")")) {
            int startIdx = fullAddress.indexOf("(");
            int endIdx = fullAddress.indexOf(")");
            extraAddress = fullAddress.substring(startIdx, endIdx + 1);
            fullAddress = fullAddress.substring(0, startIdx).trim();
        }

        int lastSpaceIdx = fullAddress.lastIndexOf(" ");
        String mainAddress = fullAddress.substring(0, lastSpaceIdx).trim();
        String detailAddress = fullAddress.substring(lastSpaceIdx + 1).trim();

        addressParts.put("mainAddress", mainAddress);
        addressParts.put("detailAddress", detailAddress);
        addressParts.put("extraAddress", extraAddress);

        return addressParts;
    }

    public Pair<ReportsPageVo, InquiriesEntity[]> selectInquiries(int page) {
        page = Math.max(page, 1);
        int totalCount = this.inquiriesArticleMapper.selectInquiriesCount();
        ReportsPageVo inquiriesPage = new ReportsPageVo(page, totalCount);
        InquiriesEntity[] inquiries = this.inquiriesArticleMapper.selectInquiries(inquiriesPage.countPerPage, inquiriesPage.offsetCount);
        return Pair.of(inquiriesPage, inquiries);
    }
}