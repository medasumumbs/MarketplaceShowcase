package ru.muravin.marketplaceshowcase.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.muravin.marketplaceshowcase.services.ProductsService;

@Controller
@RequestMapping("/products")
public class ProductsController {
    private final ProductsService productsService;

    @Autowired
    public ProductsController(ProductsService productsService){
        this.productsService = productsService;
    }

    @GetMapping
    @Transactional
    public ModelAndView getProducts(@RequestParam(required = false) String search,
                                    @RequestParam(required = false) String sort,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(defaultValue = "1") Integer pageNumber
    ) {
        ModelAndView modelAndView = new ModelAndView("main.html");
        modelAndView.addObject("products", productsService.findAll(PageRequest.of(pageNumber-1, pageSize)));
        modelAndView.addObject("search", search);
        modelAndView.addObject("sort", sort);
        modelAndView.addObject("pageSize", pageSize);
        modelAndView.addObject("pageNumber", pageNumber);
        modelAndView.addObject("lastPageNumber", Math.ceil((double)productsService.countAll()/pageSize));

        return modelAndView;
    }
}
