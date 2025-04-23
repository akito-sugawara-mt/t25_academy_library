package jp.co.metateam.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;
import lombok.extern.log4j.Log4j2;

/**
 * 書籍関連クラス
 */
@Log4j2
@Controller
public class BookController {
    
    private final BookMstService bookMstService;

    @Autowired
    public BookController(BookMstService bookMstService){
        this.bookMstService = bookMstService;
    }

    @GetMapping("/book/index")
    public String index(Model model) {
        // 書籍を全件取得
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
        
        model.addAttribute("bookMstList", bookMstList);

        return "book/index";
    }

    @GetMapping("/book/add")
    public String add(Model model) {
        if (!model.containsAttribute("bookMstDto")) {
            model.addAttribute("bookMstDto", new BookMstDto());
        }

        return "book/add";
    }

    @PostMapping("/book/add")
    public String register(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra){
        try{
            boolean errTitleFlg = false;
            boolean errIsbnFlg = false;
            String titleExist =(bookMstDto.getTitle());
            String isbnExist = (bookMstDto.getIsbn());
            // if(isbnExist != null){
            //     result.rejectValue("isbn", "error.value", "既に登録済みのISBNです");
            //     errIsbnFlg = true;
            // }
            if(titleExist == null || titleExist.isEmpty()){
                result.rejectValue("title", "error.value", "書籍名は必須です");
                errTitleFlg = true;
            }
            if(isbnExist == null || isbnExist.isEmpty()){
                result.rejectValue("isbn", "error.value", "ISBNは必須です");
                errIsbnFlg = true;
            }
            if(titleExist.length()>100){
                result.rejectValue("title", "error.value", "書籍名は100文字以内で入力してください");
                errTitleFlg = true;
            }
            if(isbnExist.length() != 13){
                result.rejectValue("isbn", "error.value", "ISBNは13文字で入力してください");
                errIsbnFlg = true;
            }
            if(!isbnExist.matches("^[\\p{ASCII}]*$") ){
                result.rejectValue("isbn", "error.value", "ISBNは半角文字で入力してください");
                errIsbnFlg = true;
            }
            if(bookMstService.searchIsbn(isbnExist) != null && !bookMstService.searchIsbn(isbnExist).isEmpty()){
                result.rejectValue("isbn", "error.value", "既に登録済みのISBNです");
                errIsbnFlg = true;
            }
            // if(isbnExist.existsBy(bookMstService)){
            //     result.rejectValue("isbn", "error.value", "既に登録済みのISBNです");
            //     errIsbnFlg = true;
            // }
            if (errTitleFlg || errIsbnFlg) {
                throw new Exception("Book already exists.");
            }
            // if(result.hasErrors()|| errIsbnFlg || errTitleFlg){
            //     ra.addFlashAttribute("bookMstDto", bookMstDto);
            //     ra.addFlashAttribute("org.springframework.validation.BindingResult.bookMstDto", result);
            // }
            bookMstService.save(bookMstDto);
            return"redirect:/book/index";
         }
            catch( Exception e){
                log.error("Error during book registration:" + e.getMessage());
    
                ra.addFlashAttribute("bookMstDto",bookMstDto);
                ra.addFlashAttribute("org.springframework.validation.BindingResult.bookMstDto",result);

                return "redirect:/book/add";
            }
         
    }
 }
    

