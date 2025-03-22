package cn.bugstack.mall.search.controller;

import cn.bugstack.mall.search.service.MallSearchService;
import cn.bugstack.mall.search.vo.SearchParamVO;
import cn.bugstack.mall.search.vo.SearchResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/5 01:06
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    /**
     * 搜索功能
     */
    @GetMapping("/list.html")
    public String listPage(SearchParamVO searchParam, Model model, HttpServletRequest request) {
        String queryString = request.getQueryString();
        searchParam.set_queryString(queryString);
        SearchResponseVO result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}
