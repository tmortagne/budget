package org.mortagne.budget.internal.transation.io.lcl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.mortagne.budget.transation.Transaction;
import org.mortagne.budget.transation.io.TransactionReader;
import org.mortagne.budget.transation.io.TransactionReaderConfiguration;
import org.mortagne.budget.transation.io.TransactionReaderFactory;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

class BudgetService
{
    private XWikiContext context;

    private Map<String, String> filters;

    public BudgetService(XWikiContext context)
    {
        this.context = context;
    }

    public String getAccountSpace(String account)
    {
        return "Account" + account;
    }

    public Map<String, String> getFilters() throws QueryException
    {
        if (this.filters == null) {
            this.filters = new HashMap<String, String>();

            QueryManager queryManager = Utils.getComponent(QueryManager.class);

            Query query =
                queryManager
                    .createQuery(
                        "select filter.pattern, filter.category from Document doc, doc.object(BudgetCode.AccountTransactionFilterClass) as filter",
                        Query.XWQL);

            List<Object[]> filtersDatas = query.execute();

            for (Object[] filterDatas in filtersDatas) {
                this.filters.put((String) filterDatas[0], (String) filterDatas[1]);
            }
        }

        return this.filters;
    }

    private String getCategory(String description)
    {
        Map<String, String> filters;
        try {
            filters = getFilters();
        } catch (QueryException e) {
            return null;
        }

        for (Map.Entry<String, String> entry in filters.entrySet()) {
            if (Pattern.matches(entry.getKey(), description)) {
                return entry.getValue();
            }
        }

        return null;
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

    public XWikiDocument getAccountTransactionTemplate() throws XWikiException
    {
        return this.context.getWiki().getDocument(
            new DocumentReference(context.getDatabase(), "BudgetCode", "AccountTransactionClassTemplate"), context);
    }

    public DocumentReference registerTransation(Transaction transaction, String account, boolean force)
        throws XWikiException
    {
        XWikiDocument document =
            this.context.getWiki().getDocument(
                getTransactionDocument(this.context.getDatabase(), account, transaction), this.context);

        //if (force || document.isNew()) {
            XWikiDocument template = getAccountTransactionTemplate();

            if (!template.isNew()) {
                document.setTitle(template.getTitle());
                document.setContent(template.getContent());
                document.setSyntax(template.getSyntax());
                document.mergeXObjects(template);
            }

            document.setParentReference(new EntityReference("WebHome", EntityType.DOCUMENT));

            BaseObject xtransaction =
                document.getXObject(new DocumentReference(this.context.getDatabase(), "BudgetCode",
                    "AccountTransactionClass"), true, this.context);

            xtransaction.setStringValue("account", account);
            xtransaction.setStringValue("description", transaction.getDescription());
            xtransaction.setStringValue("type", transaction.getType());
            xtransaction.setDateValue("date", transaction.getDate());
            xtransaction.setDateValue("realdate", transaction.getRealDate() != null ? transaction.getRealDate()
                : transaction.getDate());
            xtransaction.setFloatValue("value", transaction.getValue());

            xtransaction.setStringValue("category", getCategory(transaction.getDescription()));

            this.context.getWiki().saveDocument(document, this.context);
        //}

        return document.getDocumentReference();
    }
}
