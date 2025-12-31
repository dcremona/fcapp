package fcweb.utils;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public class CustomMessageDialog{

	public static final String LABEL_CHIUDI = "Chiudi";
	//public static final String LABEL_ANNULLA = "Annulla";
	public static final String LABEL_SALVA = "Salva";

	public static final String MSG_OK = "Operazione eseguita con successo!";

	public static final String MSG_MAIL_KO = "Errore invio mail";
	public static final String MSG_ADMIN_MERCATO_KO = "L'amministratore non ha ancora abilitato il mercato.";
	public static final String MSG_ERROR_GENERIC = "Errore nel sistema, contattare amministratore";

	public static final String MSG_ERROR_INSERT_GIOCATORI = "Attenzione, impostare tutti i giocatori";

	public static final String TITLE_MSG_CONFIRM = "Per favore conferma:";
	public static final String TITLE_MSG_ERROR = "Errore";
	public static final String TITLE_MSG_INFO = "Info";

	public static void showMessageError(String msg) {

		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setHeader(TITLE_MSG_ERROR);
		dialog.setText(msg == null ? "ND" : msg);
		dialog.setConfirmText(LABEL_CHIUDI);
		dialog.open();
	}

	public static void showMessageInfo(String msg) {

		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setHeader(TITLE_MSG_INFO);
		dialog.setText(msg == null ? "ND" : msg);

		dialog.setConfirmText(LABEL_CHIUDI);
		dialog.open();
	}

	public static void showMessageErrorDetails(String msg, String details) {

		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setHeader(TITLE_MSG_ERROR);
		dialog.setText((msg == null ? "ND" : msg) + " " + (details == null ? "ND" : details));

		dialog.setConfirmText(LABEL_CHIUDI);
		dialog.open();

	}

}
