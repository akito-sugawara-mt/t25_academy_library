package jp.co.metateam.library.service;

import java.sql.Timestamp; 
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.repository.BookMstRepository;

@Service
public class BookMstService {

    private final BookMstRepository bookMstRepository;
    
    @Autowired
    public BookMstService(BookMstRepository bookMstRepository){
        this.bookMstRepository = bookMstRepository;
    }
    
    public List<BookMstDto> findAvailableWithStockCount() {
        List<BookMst> books = this.bookMstRepository.findLimitedBook();
        List<BookMstDto> bookMstDtoList = new ArrayList<BookMstDto>();

        // 書籍の在庫数を取得
        // FIXME: 現状は書籍ID毎にDBに問い合わせている。一度のSQLで完了させたい。
        for (int i = 0; i < books.size(); i++) {
            BookMst book = books.get(i);
            BookMstDto bookMstDto = new BookMstDto();
            bookMstDto.setId(book.getId());
            bookMstDto.setIsbn(book.getIsbn());
            bookMstDto.setTitle(book.getTitle());
            bookMstDtoList.add(bookMstDto);
        }

        return bookMstDtoList;
    }
    public String searchIsbn(String isbn) {
        Optional<BookMst> bookMstOptional = bookMstRepository.selectByisbn(isbn);
        if (bookMstOptional.isPresent()) {
            return bookMstOptional.get().getIsbn();
        } else {
            return null;
        }
    }

    @Transactional
    public void save(BookMstDto bookMstDto) {
        try {

            BookMst bookMst = new BookMst();

            bookMst.setTitle(bookMstDto.getTitle());
            bookMst.setIsbn(bookMstDto.getIsbn());
           ;

            // データベースへの保存
            this.bookMstRepository.save(bookMst);
        } catch (Exception e) {
            throw e;
        }
    }
    public BookMstDto findById(Long id) {
        BookMst entity = bookMstRepository.findById(id).orElse(null);
        if (entity == null) return null;
     
        BookMstDto dto = new BookMstDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setIsbn(entity.getIsbn());
        return dto;
    }

     @Transactional
    public void update(BookMstDto bookMstDto) {
        try {

            BookMst bookMst = bookMstRepository.findById(bookMstDto.getId())
            .orElseThrow(() -> new RuntimeException("書籍が見つかりません"));

            bookMst.setTitle(bookMstDto.getTitle());
            bookMst.setIsbn(bookMstDto.getIsbn());

            // データベースへの保存
            this.bookMstRepository.save(bookMst);
        } catch (Exception e) {
            throw e;
        }
    }
    public BookMstDto findAvailableById(Long id) {
        Optional<BookMst> entityOpt = bookMstRepository.findByIdAndDeletedFlgFalse(id);
        if (entityOpt.isEmpty()) return null;

        BookMst entity = entityOpt.get();
        BookMstDto dto = new BookMstDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setIsbn(entity.getIsbn());
        return dto;
    }

    @Transactional
    public void logicalDelete(Long id) {
        Optional<BookMst> bookOpt = bookMstRepository.findByIdAndDeletedFlgFalse(id);
        if (bookOpt.isPresent()) {
            BookMst book = bookOpt.get();
            book.setDeletedFlg(true);
            book.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));  
            bookMstRepository.save(book);
        }
    }
}
