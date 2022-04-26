package jp.co.seattle.library.controller;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jp.co.seattle.library.dto.BookDetailsInfo;
import jp.co.seattle.library.service.BooksService;
import jp.co.seattle.library.service.ThumbnailService;

/**
 * 編集コントローラー
 */
@Controller // APIの入り口
public class EditBookController {

	final static Logger logger = (Logger) LoggerFactory.getLogger(EditBookController.class);

	@Autowired
	private BooksService booksService;

	@Autowired
	private ThumbnailService thumbnailService;

	// 編集ボタンを押したら編集画面に遷移する
	@RequestMapping(value = "/editBook", method = RequestMethod.GET) // value＝actionで指定したパラメータ
	// RequestParamでname属性を取得
	public String editBook(int bookId, Model model) {
		model.addAttribute("bookDetailsInfo", booksService.getBookInfo(bookId));
		return "editBook";
	}

	/**
	 * 対象書籍を編集する
	 */
	@Transactional
	@RequestMapping(value = "/updateBook", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
	public String updateBook(Locale locale,@RequestParam("bookId")int bookId,@RequestParam("title") String title, @RequestParam("author") String author,
			@RequestParam("publisher") String publisher, @RequestParam("thumbnail") MultipartFile file,
			@RequestParam("publish_date") String publishDate, @RequestParam("isbn") String Isbn,
			@RequestParam("texts") String Texts, Model model) {
		logger.info("Welcome insertBooks.java! The client locale is {}.", locale);

		// パラメータで受け取った書籍情報をDtoに格納する。
		BookDetailsInfo bookInfo = new BookDetailsInfo();
		bookInfo.setBookId(bookId);
		bookInfo.setTitle(title);
		bookInfo.setAuthor(author);
		bookInfo.setPublisher(publisher);
		bookInfo.setPublishDate(publishDate);
		bookInfo.setIsbn(Isbn);
		bookInfo.setTexts(Texts);

		// クライアントのファイルシステムにある元のファイル名を設定する
		String thumbnail = file.getOriginalFilename();

		if (!file.isEmpty()) {
			try {
				// サムネイル画像をアップロード
				String fileName = thumbnailService.uploadThumbnail(thumbnail, file);
				// URLを取得
				String thumbnailUrl = thumbnailService.getURL(fileName);

				bookInfo.setThumbnailName(fileName);
				bookInfo.setThumbnailUrl(thumbnailUrl);

			} catch (Exception e) {

				// 異常終了時の処理
				logger.error("サムネイルアップロードでエラー発生", e);
				model.addAttribute("bookDetailsInfo", bookInfo);
				return "addBook";
			}
		}

		// 書籍情報を編集する

		// 必須条件が書かれているかどうかの分岐
		String error = "";

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

		// どれかerrorだとaddBookに戻る
		if (!(error.equals(""))) {
			model.addAttribute("error", error);
			model.addAttribute("bookInfo", bookInfo);
			return "addBook";
		}

		// TODO 登録した書籍の詳細情報を表示するように実装
		 booksService.editBook(bookInfo);
		 booksService.getBookInfo(bookId);
		 
		// 詳細画面に遷移する
		model.addAttribute("bookDetailsInfo",booksService.getBookInfo(bookId));
		model.addAttribute("resultMessage", "登録完了");
		return "details";
	}
}
