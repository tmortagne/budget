package org.mortagne.budget.internal.transation.io.lcl;

import org.mortagne.budget.transation.Transaction;
import org.mortagne.budget.transation.io.TransactionReader;
import org.mortagne.budget.transation.io.TransactionReaderConfiguration;
import org.mortagne.budget.transation.io.TransactionReaderFactory;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

class BudgetService
{
    XWikiContext context;

    public BudgetService(XWikiContext context)
    {
        this.context = context;
    }

    public String getAccountSpace(String account)
    {
        return "Account" + account;
    }

    public DocumentReference getTransactionDocument(String wiki, String account, Transaction transaction)
    {
        return new DocumentReference(wiki, getAccountSpace(account), transaction.getId().replace('/', '-'));
    }

    public void importAttachment(String attachmentFilename, String readerId, String account) throws Exception
    {
        TransactionReaderFactory readerFactory = Utils.getComponent(TransactionReaderFactory.class, readerId);

        XWikiAttachment attachment = this.context.getDoc().getAttachment(attachmentFilename);

        if (attachment == null) {
            throw new Exception("Can't find attachment [" + attachmentFilename + "] in document ["
                + this.context.getDoc() + "]");
        }

        TransactionReaderConfiguration configuration = new TransactionReaderConfiguration();

        TransactionReader reader =
            readerFactory.createTransactionReader(attachment.getContentInputStream(this.context), configuration);

        for (Transaction transaction = reader.next(); transaction != null; transaction = reader.next()) {
            registerTransation(transaction, account, false);
        }
    }

    public DocumentReference registerTransation(Transaction transaction, String account, boolean force)
        throws XWikiException
    {
        XWikiDocument document =
            this.context.getWiki().getDocument(
                getTransactionDocument(this.context.getDatabase(), account, transaction), this.context);

        if (force || document.isNew()) {
            BaseObject xtransaction =
                document.getXObject(new DocumentReference(this.context.getDatabase(), "BudgetCode",
                    "AccountTransactionClass"), true, this.context);

            xtransaction.setStringValue("description", transaction.getDescription());
            xtransaction.setStringValue("type", transaction.getType());
            xtransaction.setDateValue("date", transaction.getDate());
            xtransaction.setDateValue("realdate", transaction.getRealDate() != null ? transaction.getRealDate()
                : transaction.getDate());
            xtransaction.setFloatValue("value", transaction.getValue());

            this.context.getWiki().saveDocument(document, this.context);
        }

        return document.getDocumentReference();
    }
}
