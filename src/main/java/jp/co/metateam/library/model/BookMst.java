package jp.co.metateam.library.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.sql.Timestamp;

/**
 * 書籍マスタ
 */
@Entity
@Table(name = "BookMst")
public class BookMst {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** ISBN */
    @Column(name = "isbn", nullable = false, unique = true)
    private String isbn;

    /** 書籍タイトル */
    @Column(name = "title", nullable = false)
    private String title;

    /** 削除日時 */
    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    /** 論理削除フラグ */
    @Column(name = "deleted_flg", nullable = false)
    private Boolean deletedFlg = false;

    /** Getters */

    public Long getId() {
        return this.id;
    }

    public String getIsbn() {
        return this.isbn;
    }

    public String getTitle() {
        return this.title;
    }

    public Boolean getDeletedFlg() {
        return this.deletedFlg; 
    }
     public Timestamp getDeletedAt() {
         return this.deletedAt; 
    } 
    /** Setters */

    public void setId(Long id) {
        this.id = id;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDeletedFlg(Boolean deletedFlg) {
        this.deletedFlg = deletedFlg;
    }
    public void setDeletedAt(Timestamp deletedAt) {
         this.deletedAt = deletedAt; 
    } 
}
