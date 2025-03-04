package cn.bugstack.mall.product.web;

import cn.bugstack.common.utils.R;
import cn.bugstack.mall.product.entity.CategoryEntity;
import cn.bugstack.mall.product.service.CategoryService;
import cn.bugstack.mall.product.vo.CateLog2VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/3 17:45
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {

        List<CategoryEntity> categoryEntityList = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categoryEntityList);

        return "index";
    }

    @GetMapping("/index/catalog.json")
    @ResponseBody
    public Map<String, List<CateLog2VO>> getCatalogJson() {
        return categoryService.getCatalogJson();
    }
}
