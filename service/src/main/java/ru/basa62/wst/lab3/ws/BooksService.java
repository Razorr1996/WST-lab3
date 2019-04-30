package ru.basa62.wst.lab3.ws;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.basa62.wst.lab3.BooksDAO;
import ru.basa62.wst.lab3.BooksEntity;
import ru.basa62.wst.lab3.ws.exception.BooksServiceException;
import ru.basa62.wst.lab3.ws.exception.BooksServiceFault;
import ru.basa62.wst.lab3.ws.exception.ForbiddenException;
import ru.basa62.wst.lab3.ws.exception.UnuathorizedException;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor
@RequiredArgsConstructor
@WebService(serviceName = "BooksService")
public class BooksService {
    @Inject
    @NonNull
    private BooksDAO booksDAO;
    private AuthChecker authChecker = (userName, password) -> userName.equals("user") && password.equals("password");
    @Resource
    private WebServiceContext webServiceContext;

    @WebMethod
    public List<BooksEntity> findAll() throws BooksServiceException {
        try {
            return booksDAO.findAll();
        } catch (SQLException e) {
            String message = "SQL error: " + e.getMessage() + "; State:" + e.getSQLState();
            throw new BooksServiceException(message, e, new BooksServiceFault(message));
        }
    }

    @WebMethod
    public List<BooksEntity> filter(@WebParam(name = "id") Long id, @WebParam(name = "name") String name,
                                    @WebParam(name = "author") String author,
                                    @WebParam(name = "publicDate") String publicDate, @WebParam(name = "isbn") String isbn) throws BooksServiceException {
        try {
            return booksDAO.filter(id, name, author, getDate(publicDate), isbn);
        } catch (SQLException e) {
            String message = "SQL error: " + e.getMessage() + "; State:" + e.getSQLState();
            throw new BooksServiceException(message, e, new BooksServiceFault(message));
        } catch (ParseException e) {
            String message = "Parse error: " + e.getMessage();
            throw new BooksServiceException(message, e, new BooksServiceFault(message));
        }
    }

    @WebMethod
    public Long create(@WebParam(name = "name") String name,
                       @WebParam(name = "author") String author,
                       @WebParam(name = "publicDate") String publicDate, @WebParam(name = "isbn") String isbn) throws BooksServiceException, UnuathorizedException, ForbiddenException {
        checkAuth();
        try {
            return booksDAO.create(name, author, getDate(publicDate), isbn);
        } catch (SQLException e) {
            String message = "SQL error: " + e.getMessage() + ". State: " + e.getSQLState();
            throw new BooksServiceException(message, e, new BooksServiceFault(message));
        } catch (ParseException e) {
            String message = "Parse error: " + e.getMessage();
            throw new BooksServiceException(message, e, new BooksServiceFault(message));
        }
    }

    @WebMethod
    public int delete(@WebParam(name = "id") long id) throws BooksServiceException, UnuathorizedException, ForbiddenException {
        checkAuth();
        try {
            int count = booksDAO.delete(id);
            if (count == 0) {
                String message = "Book with id=" + id + " doesn't exist.";
                throw new BooksServiceException(message, new BooksServiceFault(message));
            }
            return count;
        } catch (SQLException e) {
            String message = "SQL error: " + e.getMessage() + ". State: " + e.getSQLState();
            throw new BooksServiceException(message, e, new BooksServiceFault(message));
        }
    }

    @WebMethod
    public int update(@WebParam(name = "id") Long id,
                      @WebParam(name = "name") String name,
                      @WebParam(name = "author") String author,
                      @WebParam(name = "publicDate") String publicDate, @WebParam(name = "isbn") String isbn) throws BooksServiceException, UnuathorizedException, ForbiddenException {
        checkAuth();
        try {
            int count = booksDAO.update(id, name, author, getDate(publicDate), isbn);
            if (count == 0) {
                String message = "Book with id=" + id + " doesn't exist.";
                throw new BooksServiceException(message, new BooksServiceFault(message));
            }
            return count;
        } catch (SQLException e) {
            String message = "SQL error: " + e.getMessage() + ". State: " + e.getSQLState();
            throw new BooksServiceException(message, e, new BooksServiceFault(message));
        } catch (ParseException e) {
            String message = "Parse error: " + e.getMessage();
            throw new BooksServiceException(message, e, new BooksServiceFault(message));
        }
    }

    private Date getDate(String string) throws ParseException {
        if (string == null) {
            return null;
        } else {
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            return new Date(simpleDateFormat.parse(string).getTime());
        }
    }

    private void checkAuth() throws ForbiddenException, UnuathorizedException {
        List<String> creds = decodeAuthHeader();
        String username = creds.get(0);
        String pass = creds.get(1);
        boolean check = authChecker.check(username, pass);
        if (!check) {
            String mes = "User not found";
            throw new ForbiddenException(mes, new BooksServiceFault(mes));
        }
    }

    private List<String> decodeAuthHeader() throws UnuathorizedException, ForbiddenException {
        MessageContext mctx = webServiceContext.getMessageContext();
        Map headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
        List<String> authorization = (List<String>) headers.get("Authorization");
        if (authorization == null || authorization.size() != 1) {
            throw new UnuathorizedException("No authorization header", new BooksServiceFault("No authorization header"));
        }
        String header = authorization.get(0);
        String basicRegex = "^Basic\\s+";
        Pattern compile = Pattern.compile(basicRegex);
        Matcher matcher = compile.matcher(header);
        if (!matcher.find()) {
            String mes = "Not an basic auth";
            throw new ForbiddenException(mes, new BooksServiceFault(mes));
        }
        String encodedCreds = matcher.replaceFirst("");
        String decodedCreds;
        try {
            decodedCreds = new String(Base64.getDecoder().decode(encodedCreds), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exc) {
            String mes = "Isn't base64 encoded";
            throw new ForbiddenException(mes, new BooksServiceFault(mes));
        }
        String[] split = decodedCreds.split(":");
        if (split.length != 2) {
            String mes = "Wrong header format";
            throw new ForbiddenException(mes, new BooksServiceFault(mes));
        }
        return Arrays.asList(split);
    }
}
