package jp.co.metateam.library.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.BookMst;
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
            
            String titleExist =(bookMstDto.getTitle());
            String isbnExist = (bookMstDto.getIsbn());
           
            boolean errTitleFlg = isTitleValid(result, titleExist);

            boolean errIsbnFlg = isIsbnValid(result, isbnExist);
           
            if (errTitleFlg || errIsbnFlg) {
                throw new Exception("Book already exists.");
            }
           
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

    @GetMapping("/book/edit/{id}")
    public String edit(@PathVariable ("id") long id, Model model,RedirectAttributes ra){
        BookMstDto bookMstDto = bookMstService.findById(id);
    if (bookMstDto == null){
         ra.addFlashAttribute("message", "指定された書籍が存在しません");
        return "redirect:/book/index";
    }
    model.addAttribute("bookMstDto", bookMstDto);
        return "book/edit";
    }

    @PostMapping("/book/edit")
    public String edit(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result,Model model, RedirectAttributes ra) {
        try {
            boolean errTitleFlg = false;
            boolean errIsbnFlg = false;

            // 元データ取得
            BookMstDto original = bookMstService.findById(bookMstDto.getId());
            if (original == null) {
                result.rejectValue("id", "error.value", "指定された書籍が存在しません");
                model.addAttribute("message", "指定された書籍が存在しません");
                return "book/edit";
            }

            String titleExist = bookMstDto.getTitle();
            String isbnExist = bookMstDto.getIsbn();

            // 書籍名とISBNの変更を確認
            boolean isTitleChanged = !original.getTitle().equals(titleExist);
            boolean isIsbnChanged = !original.getIsbn().equals(isbnExist);

            // 変更がなかった場合、メッセージを追加してリダイレクト
            if (!isTitleChanged && !isIsbnChanged) {
                ra.addFlashAttribute("message", "変更がありませんでした");
                return "redirect:/book/edit/" + bookMstDto.getId();
            }

            // 書籍名のバリデーション（変更があった場合のみ）
            if (isTitleChanged) {
                errTitleFlg = isTitleValid(result, titleExist);
            }

            // ISBNのバリデーション（変更があった場合のみ）
            if (isIsbnChanged) {
               errIsbnFlg = isIsbnValid(result,  isbnExist);
            }

            // バリデーションエラーがあった場合
            if (result.hasErrors()) {
            return "book/edit";
            }

            // 保存処理
            bookMstService.update(bookMstDto);
            ra.addFlashAttribute("message", "変更されました");
            return "redirect:/book/index";

        } catch (Exception e) {
            log.error("Error during book edit: " + e.getMessage());
            ra.addFlashAttribute("bookMstDto", bookMstDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.bookMstDto", result);
            return "book/edit";
        }
    }

    private boolean isTitleValid(BindingResult result, String titleExist) {
        boolean errTitleFlg=false;
        if(titleExist == null || titleExist.isEmpty()){
            result.rejectValue("title", "error.value", "書籍名は必須です");
            errTitleFlg = true;
        }
        if(titleExist.length()>255){
            result.rejectValue("title", "error.value", "書籍名は255文字以内で入力してください");
            errTitleFlg = true;
        }
        return errTitleFlg;
    }

    private boolean isIsbnValid(BindingResult result, String isbnExist) {
        boolean errIsbnFlg=false;
        if(isbnExist == null || isbnExist.isEmpty()){
            result.rejectValue("isbn", "error.value", "ISBNは必須です");
            errIsbnFlg = true;
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
        return errIsbnFlg;
    }

    @PostMapping("/book/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
    BookMstDto book = bookMstService.findAvailableById(id);
    if (book == null) {
        ra.addFlashAttribute("errorMessage", "指定された書籍が存在しません");
        return "redirect:/book/index";
    }
    bookMstService.logicalDelete(id); 
    ra.addFlashAttribute("successMessage", "削除が完了しました");
    return "redirect:/book/index";
    }
}
