package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.*;
import com.yhkim.fave.services.AdminPageService;
import com.yhkim.fave.services.FaveService;
import com.yhkim.fave.vos.BoardPostPageVo;
import com.yhkim.fave.vos.IndexPageVo;
import com.yhkim.fave.vos.ReportsPageVo;
import com.yhkim.fave.vos.UserPageVo;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/admin")
public class AdminPageController {

    private final AdminPageService adminPageService;
    private final FaveService faveService;

    @Autowired
    public AdminPageController(AdminPageService adminPageService, FaveService faveService) {
        this.adminPageService = adminPageService;
        this.faveService = faveService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getIndex(@RequestParam(value = "userPage", required = false, defaultValue = "1") int userPage,
                                 @RequestParam(value = "boardPage", required = false, defaultValue = "1") int boardPage,
                                 @RequestParam(value = "reportPage", required = false, defaultValue = "1") int reportPage,
                                 @RequestParam(value = "inquiriesPage", required = false, defaultValue = "1") int inquiriesPage) {
        Pair<IndexPageVo, UserEntity[]> user = this.adminPageService.selectIndexUser(userPage);
        Pair<IndexPageVo, BoardPostEntity[]> board = this.adminPageService.selectIndexBoard(boardPage);
        Pair<IndexPageVo, ReportEntity[]> reports = this.adminPageService.selectIndexReport(reportPage);
        System.out.println("유저페이지:"+userPage);
        System.out.println("보드페이지:"+boardPage);
        System.out.println("리폿페이지:"+reportPage);
        Pair<IndexPageVo, InquiriesEntity[]> Inquiries = this.adminPageService.selectAllInquiries(inquiriesPage);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("userPage", user.getLeft());
        modelAndView.addObject("user", user.getRight());
        modelAndView.addObject("boardPage", board.getLeft());
        modelAndView.addObject("board", board.getRight());
        modelAndView.addObject("reportsPage", reports.getLeft());
        modelAndView.addObject("reports", reports.getRight());
        modelAndView.addObject("inquiriesPage", Inquiries.getLeft());
        modelAndView.addObject("inquiries", Inquiries.getRight());
        modelAndView.setViewName("admin/adminIndex");
        return modelAndView;
    }

    @RequestMapping(value = "write/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getWrite() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/adminWrite");
        return modelAndView;
    }

    @RequestMapping(value = "write/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> postWrite(@RequestParam("title") String title,
                                       @RequestParam("location") String location,
                                       @RequestParam("startDate") String startDate,
                                       @RequestParam("endDate") String endDate,
                                       @RequestParam("description") String description,
                                       @RequestParam("coverData") MultipartFile coverFile) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDateTime = LocalDate.parse(startDate, formatter);
        LocalDate endDateTime = LocalDate.parse(endDate, formatter);

        FaveInfoEntity adminPage = new FaveInfoEntity();
        adminPage.setTitle(title);
        adminPage.setLocation(location);
        adminPage.setStartDate(startDateTime);
        adminPage.setEndDate(endDateTime);
        adminPage.setDescription(description);

        Boolean result = this.adminPageService.write(adminPage, coverFile);
        System.out.println(result);
        Map<String, String> response = new HashMap<>();
        if (result) {
            response.put("result", result.toString());
            return ResponseEntity.ok(response);
        } else {
            response.put("result", result.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @RequestMapping(value = "modify/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getModify(@RequestParam(value = "index", required = false) int index) {
        ModelAndView modelAndView = new ModelAndView();
        FaveInfoEntity fave = this.faveService.selectFaveInfoById(index);
        Map<String, String> addressParts = this.adminPageService.splitAddress(fave.getLocation());
        modelAndView.addObject("mainAddress", addressParts.get("mainAddress"));
        modelAndView.addObject("detailAddress", addressParts.get("detailAddress"));
        modelAndView.addObject("extraAddress", addressParts.get("extraAddress"));
        modelAndView.addObject("fave", fave);
        modelAndView.setViewName("admin/adminModify");
        return modelAndView;
    }

    @RequestMapping(value = "modify/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> postModify(
            @RequestParam("index") Integer index,
            @RequestParam("title") String title,
            @RequestParam("location") String location,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("description") String description,
            @RequestParam(value = "coverData", required = false) MultipartFile coverFile,
            @RequestParam(value = "deleteCover", required = false) Boolean deleteCover) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDateTime = LocalDate.parse(startDate, formatter);
        LocalDate endDateTime = LocalDate.parse(endDate, formatter);

        FaveInfoEntity adminPage = new FaveInfoEntity();
        adminPage.setIndex(index);
        adminPage.setTitle(title);
        adminPage.setLocation(location);
        adminPage.setStartDate(startDateTime);
        adminPage.setEndDate(endDateTime);
        adminPage.setDescription(description);

        Boolean result = this.adminPageService.modify(adminPage, coverFile, deleteCover);
        Map<String, String> response = new HashMap<>();
        response.put("result", result.toString());

        if (result) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @RequestMapping(value = "user/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getUser(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                @RequestParam(value = "filter", required = false) String filter,
                                @RequestParam(value = "keyword", required = false) String keyword) {
        ModelAndView modelAndView = new ModelAndView();
        if (keyword == null) {
            Pair<UserPageVo, UserEntity[]> pair = this.adminPageService.selectUserPage(page);
            System.out.println("pair:" + Arrays.toString(pair.getRight()));
            modelAndView.addObject("page", pair.getLeft());
            modelAndView.addObject("user", pair.getRight());
        } else {
            Pair<UserPageVo, UserEntity[]> pair = this.adminPageService.selectUserPageBySearch(filter, keyword, page);
            modelAndView.addObject("page", pair.getLeft());
            modelAndView.addObject("user", pair.getRight());
            modelAndView.addObject("filter", filter);
            modelAndView.addObject("keyword", keyword);
        }
        modelAndView.setViewName("admin/adminUser");
        return modelAndView;
    }

    @RequestMapping(value = "delete/", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchDelete(@RequestParam(value = "userEmail", required = false) String userEmail, @RequestParam(value = "index", required = false) Integer index) {
        JSONObject response = new JSONObject();
        if (userEmail != null) {
            Boolean result = this.adminPageService.updateDeleted(userEmail);
            response.put("result", result);
        }
        if (index != null) {
            Boolean result = this.adminPageService.deleteBoardPost(index);
            response.put("result", result);
        }
        return response.toString();
    }

    @RequestMapping(value = "warning/", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchWarning(@RequestParam(value = "userEmail", required = false) String userEmail, @RequestParam(value = "warning", required = false, defaultValue = "0") int warning) {
        Boolean result = this.adminPageService.updateWarning(userEmail, warning);
        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @RequestMapping(value = "board/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getBoard(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                 @RequestParam(value = "filter", required = false) String filter,
                                 @RequestParam(value = "keyword", required = false) String keyword) {
        ModelAndView modelAndView = new ModelAndView();
        if (keyword == null) {
            Pair<BoardPostPageVo, BoardPostEntity[]> pair = this.adminPageService.selectBoardPost(page);
            modelAndView.addObject("page", pair.getLeft());
            modelAndView.addObject("board", pair.getRight());
        } else {
            Pair<BoardPostPageVo, BoardPostEntity[]> pair = this.adminPageService.selectBoardPostBySearch(filter, keyword, page);
            modelAndView.addObject("page", pair.getLeft());
            modelAndView.addObject("board", pair.getRight());
            modelAndView.addObject("filter", filter);
            modelAndView.addObject("keyword", keyword);
        }
        modelAndView.setViewName("admin/adminBoard");
        return modelAndView;
    }

    @RequestMapping(value = "reports/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getReports(@RequestParam(value = "userEmail", required = false) String userEmail,
                                   @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        ModelAndView modelAndView = new ModelAndView();
        Pair<ReportsPageVo, ReportEntity[]> pair = this.adminPageService.selectReports(page);
        modelAndView.addObject("page", pair.getLeft());
        modelAndView.addObject("reports", pair.getRight());
        modelAndView.setViewName("admin/adminReports");
        return modelAndView;
    }

    @RequestMapping(value = "reports/index", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postReports(@RequestParam(value = "index", required = false) Integer index) {
        Boolean result = this.adminPageService.updateReport(index);
        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @RequestMapping(value = "festival/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getFestival(@RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        ModelAndView modelAndView = new ModelAndView();
        Pair<UserPageVo, FaveInfoEntity[]> pair = this.adminPageService.selectFaveInfo(page);
        modelAndView.addObject("page", pair.getLeft());
        modelAndView.addObject("fave", pair.getRight());
        modelAndView.setViewName("admin/adminFave");
        return modelAndView;
    }

    @RequestMapping(value = "inquiries/", method = RequestMethod.GET)
    public ModelAndView getInquiries(@RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        ModelAndView modelAndView = new ModelAndView();
        Pair<ReportsPageVo, InquiriesEntity[]> pair = this.adminPageService.selectInquiries(page);
        modelAndView.addObject("page", pair.getLeft());
        modelAndView.addObject("inquiries", pair.getRight());
        modelAndView.setViewName("admin/adminInquiries");
        return modelAndView;
    }
}