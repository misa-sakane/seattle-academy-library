package jp.co.seattle.library.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import jp.co.seattle.library.dto.BookDetailsInfo;
import jp.co.seattle.library.dto.BookInfo;
import jp.co.seattle.library.rowMapper.BookDetailsInfoRowMapper;
import jp.co.seattle.library.rowMapper.BookInfoRowMapper;

/**
 * 書籍サービス
 * 
 * booksテーブルに関する処理を実装する
 */
@Service
public class BooksService {
	final static Logger logger = LoggerFactory.getLogger(BooksService.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * 書籍リストを取得する
	 *
	 * @return 書籍リスト
	 */
	public List<BookInfo> getBookList() {

		// TODO 取得したい情報を取得するようにSQLを修正
		List<BookInfo> getedBookList = jdbcTemplate.query(
				"select id,title,author,publisher,publish_date,thumbnail_url from books order by title  ",
				new BookInfoRowMapper());
		return getedBookList;
	}

	/**
	 * 書籍IDに紐づく書籍詳細情報を取得する
	 *
	 * @param bookId 書籍ID
	 * @return 書籍情報
	 */
	public BookDetailsInfo getBookInfo(int bookId) {

		// JSPに渡すデータを設定する
		String sql = "SELECT * FROM books where id =" + bookId;

		BookDetailsInfo bookDetailsInfo = jdbcTemplate.queryForObject(sql, new BookDetailsInfoRowMapper());

		return bookDetailsInfo;
	}

	/**
	 * 書籍を登録する
	 *
	 * @param bookInfo 書籍情報
	 */
	public void registBook(BookDetailsInfo bookInfo) {

		String sql = "INSERT INTO books (title, author,publisher,publish_date,thumbnail_name,thumbnail_url,reg_date,upd_date,texts,isbn) VALUES ('"
				+ bookInfo.getTitle() + "','" + bookInfo.getAuthor() + "','" + bookInfo.getPublisher() + "','"
				+ bookInfo.getPublishDate() + "','" + bookInfo.getThumbnailName() + "','" + bookInfo.getThumbnailUrl()
				+ "','" + "now()','" + "now()','" + bookInfo.getTexts() + "','" + bookInfo.getIsbn() + "');";

		jdbcTemplate.update(sql);
	}

	/**
	 * 書籍を削除する
	 * 
	 * @param bookInfo 書籍情報
	 */

	public void deleteBook(Integer bookId) {
		String sql = "DELETE FROM books WHERE id =" + bookId;
		jdbcTemplate.update(sql);

	}
	/**
	 * 書籍情報のバリデーションチェックをする
	 */
	public String validationcheck(String title,String author,String publisher,String publishDate,String Isbn,Model model) {
		String error = "";
		// 必須条件が書かれているかどうかの分岐
		if (title.equals("") || author.equals("") || publisher.equals("") || publishDate.equals("")) {
			error += "必須条件を書いてください。<br>";
		}

		// 出版日がYYYYMMDD形式かどうかの分岐
		if (!(publishDate.matches("(\\d{4})(\\d{2})(\\d{2})"))) {
			error += "出版日は半角数字のYYYYMMDD形式で入力してください。<br>";
		}

		// isbnが10字または13文字以内で半角数字かどうかの分岐
		if (!Isbn.equals("") && (!(Isbn.length() == 10) && !(Isbn.length() == 13) || !Isbn.matches("^[0-9]*$"))) {
			error += "ISBNの桁数または半角数字が正しくありません。";
		}
		return error;
	}
	
	/**
	 * 新規登録した書籍の情報を取得する
	 */
	public BookDetailsInfo newBook() {
		String sql = "SELECT * FROM books WHERE id = (SELECT MAX(id) FROM books);";
		BookDetailsInfo bookDetailsInfo = jdbcTemplate.queryForObject(sql, new BookDetailsInfoRowMapper());
		return bookDetailsInfo;
	}

	/**
	 * 書籍を編集する
	 */

	public void editBook(BookDetailsInfo bookInfo) {
		String sql;

		if (bookInfo.getThumbnailUrl() == null) {

			sql = "update books set title ='" + bookInfo.getTitle() + "', author ='" + bookInfo.getAuthor()
					+ "' , publisher ='" + bookInfo.getPublisher() + "', publish_date ='" + bookInfo.getPublishDate()
					+ "' , upd_date = 'now()'" + ",isbn = '" + bookInfo.getIsbn() + "', texts = '" + bookInfo.getTexts()
					+ "' where id =" + bookInfo.getBookId() + ";";

		} else {

			sql = "update books set title ='" + bookInfo.getTitle() + "', author ='" + bookInfo.getAuthor()
					+ "' , publisher ='" + bookInfo.getPublisher() + "', publish_date ='" + bookInfo.getPublishDate()
					+ "' , thumbnail_url ='" + bookInfo.getThumbnailUrl() + "', thumbnail_name ='"
					+ bookInfo.getThumbnailName() + "' , upd_date = 'now()'" + ",isbn = '" + bookInfo.getIsbn()
					+ "', texts = '" + bookInfo.getTexts() + "' where id =" + bookInfo.getBookId() + ";";
		}
		jdbcTemplate.update(sql);
	}

}