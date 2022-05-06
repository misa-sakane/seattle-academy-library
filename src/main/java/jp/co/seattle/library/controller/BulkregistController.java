package jp.co.seattle.library.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
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

@Controller // APIの入り口
public class BulkregistController {
	final static Logger logger = LoggerFactory.getLogger(AddBooksController.class);

	@Autowired
	private BooksService booksService;

	@RequestMapping(value = "/bulkregist", method = RequestMethod.GET) // value＝actionで指定したパラメータ
	// RequestParamでname属性を取得
	public String bulkregist(Model model) {
		return "bulkregist";
	}

	/**
	 * 対象書籍を一括登録する
	 */
	@Transactional
	@RequestMapping(value = "/bulkBook", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
	public String bulkBook(Locale locale,@RequestParam("upload_file") MultipartFile uploadFile, Model model) {
		logger.info("Welcome insertBooks.java! The client locale is {}.", locale);

		// パラメータで受け取った書籍情報をDtoに格納する。
		BookDetailsInfo bookInfo = new BookDetailsInfo();
		
				
		List<String[]> booksList = new ArrayList<String[]>();
		List<String> errorList = new ArrayList<String>();
		String line = null;
		boolean line2 = true;
		int count = 0;

		try {
			InputStream stream = uploadFile.getInputStream();
			Reader reader = new InputStreamReader(stream);
			BufferedReader buf = new BufferedReader(reader);

			if(!buf.ready()) {
				model.addAttribute("errorMessage", "CSVファイルが読み込めません");
				return "bulkregist";
			}
			while ((line = buf.readLine()) != null) {
				count++;
				final String[] split = line.split(",", -1);

				// バリデーションチェック
				String error = booksService.validationcheck(split[0], split[1], split[2], split[3], split[4], model);

				// 書籍リストにセット
				booksList.add(split);

				// 1つでもエラーが出たらエラー文言をセットする
				if (!(error.equals(""))) {
					errorList.add(count +"行目の書籍登録でエラーが起きました<br>");
				}

			}

		} catch (IOException e) {
			throw new RuntimeException("ファイルが読み込めません", e);

		}

		// エラーメッセージが空かどうか
		if (errorList.size() > 0) {
			model.addAttribute("errorMessage", errorList);
			return "bulkregist";
		}
		
		// 一括登録する
		for (int i =0;i<booksList.size(); i++) {
			String[]bookList = booksList.get(i);
			
			bookInfo.setTitle(bookList[0]);
			bookInfo.setAuthor(bookList[1]);
			bookInfo.setPublisher(bookList[2]);
			bookInfo.setPublishDate(bookList[3]);
			bookInfo.setIsbn(bookList[4]);
			bookInfo.setTexts(bookList[5]);
			
			booksService.registBook(bookInfo);
		}
		model.addAttribute("bookList", booksService.getBookList());
		return "home";

	}
}