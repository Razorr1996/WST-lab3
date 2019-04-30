package ru.basa62.wst.lab3;

import ru.basa62.wst.lab3.ws.client.*;
import ru.basa62.wst.lab3.ws.client.BooksEntity;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Collections.singletonList;

public class Client {
    private static BooksService service;
    private static String username = null;
    private static String password = null;

    public static void main(String[] args) throws IOException {
        URL url = new URL(args[0]);
        service = new BooksService_Service(url).getBooksServicePort();

        System.out.println("Добро пожаловать в библиотеку.");
        Scanner in = new Scanner(System.in);
        String choiceStr;
        while (true) {
            System.out.println("\nМеню:\n0 - Выход\n" +
                    "1 - findAll\n" +
                    "2 - filter\n" +
                    "3 - create\n" +
                    "4 - update\n" +
                    "5 - delete\n" +
                    "6 - Make makeAuth");
            System.out.print("Выбор: ");
            choiceStr = checkEmpty(in.nextLine());
            if (choiceStr != null) {
                int choice = Integer.parseInt(choiceStr);
                switch (choice) {
                    case 0:
                        return;
                    case 1:
                        findAll();
                        break;
                    case 2:
                        filter();
                        break;
                    case 3:
                        create();
                        break;
                    case 4:
                        update();
                        break;
                    case 5:
                        delete();
                        break;
                    case 6:
                        makeAuth();
                        break;
                    default:
                        System.out.println("Неверное значение");
                        break;
                }
            } else {
                System.out.println("Введите число от 1 до 5");
            }
        }

    }

    private static void makeAuth() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter user and password");
        while (username == null || password == null) {
            System.out.print("user: ");
            username = checkEmpty(in.nextLine());
            System.out.print("password: ");
            password = checkEmpty(in.nextLine());
        }
    }

    private static void preAuth() {
        if (username == null || password == null){
            makeAuth();
        }
        BindingProvider bp = (BindingProvider) service;
        Map<String, Object> rc = bp.getRequestContext();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Authorization", singletonList("Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8))));
        rc.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
    }

    private static void findAll() {
        System.out.println("Выведем все книги:");
        try {
            List<BooksEntity> books1 = service.findAll();
            for (BooksEntity book : books1) {
                System.out.println(printBook(book));
            }
        } catch (BooksServiceException e) {
            System.out.println(e.getFaultInfo().getMessage());
        }
    }

    private static void filter() {
        Scanner in = new Scanner(System.in);
        System.out.println("Поищем книги:");
        System.out.print("ID: ");
        String idStr = checkEmpty(in.nextLine());
        Long id = null;
        if (idStr != null) {
            id = Long.parseLong(idStr);
        }
        System.out.print("Название: ");
        String name = checkEmpty(in.nextLine());

        System.out.print("Автор: ");
        String author = checkEmpty(in.nextLine());

        System.out.print("Дата публикации (yyyy-MM-dd): ");
        String publicDate = checkEmpty(in.nextLine());

        System.out.print("ISBN: ");
        String isbn = checkEmpty(in.nextLine());

        try {
            List<BooksEntity> books2 = service.filter(id, name, author, publicDate, isbn);

            if (books2.size() == 0) {
                System.out.println("Ничего не найдено");
            } else {
                System.out.println("Найдено:");
                for (BooksEntity book : books2) {
                    System.out.println(printBook(book));
                }
            }
        } catch (BooksServiceException e) {
            System.out.println(e.getFaultInfo().getMessage());
        }
    }

    private static void create() {
        preAuth();
        Scanner in = new Scanner(System.in);
        System.out.println("Создадим книгу:");

        System.out.print("Название: ");
        String name = checkEmpty(in.nextLine());

        System.out.print("Автор: ");
        String author = checkEmpty(in.nextLine());

        System.out.print("Дата публикации (yyyy-MM-dd): ");
        String publicDate = checkEmpty(in.nextLine());

        System.out.print("ISBN: ");
        String isbn = checkEmpty(in.nextLine());

        try {
            Long newId = service.create(name, author, publicDate, isbn);
            System.out.printf("Новый ID: %d", newId);
        } catch (BooksServiceException e) {
            System.out.println(e.getFaultInfo().getMessage());
        } catch (UnuathorizedException e) {
            System.out.println(e.getFaultInfo().getMessage());
            makeAuth();
        } catch (ForbiddenException e) {
            System.out.println(e.getFaultInfo().getMessage());
            makeAuth();
        }
    }

    private static void update() {
        preAuth();
        Scanner in = new Scanner(System.in);
        System.out.println("Обновим книгу:");
        System.out.print("ID: ");

        String idStr = checkEmpty(in.nextLine());
        Long id = null;
        if (idStr != null) {
            id = Long.parseLong(idStr);
        }

        System.out.print("Название: ");
        String name = checkEmpty(in.nextLine());

        System.out.print("Автор: ");
        String author = checkEmpty(in.nextLine());

        System.out.print("Дата публикации (yyyy-MM-dd): ");
        String publicDate = checkEmpty(in.nextLine());

        System.out.print("ISBN: ");
        String isbn = checkEmpty(in.nextLine());

        try {
            int count = service.update(id, name, author, publicDate, isbn);
            System.out.printf("Обновлено: %d", count);
        } catch (BooksServiceException e) {
            System.out.println(e.getFaultInfo().getMessage());
        } catch (UnuathorizedException e) {
            System.out.println(e.getFaultInfo().getMessage());
            makeAuth();
        } catch (ForbiddenException e) {
            System.out.println(e.getFaultInfo().getMessage());
            makeAuth();
        }
    }

    private static void delete() {
        preAuth();
        Scanner in = new Scanner(System.in);
        System.out.println("Удалим книгу:");
        System.out.print("ID: ");
        String idStr = checkEmpty(in.nextLine());
        if (idStr != null) {
            long id = Long.parseLong(idStr);
            try {
                int count = service.delete(id);
                System.out.printf("Удалено: %d", count);
            } catch (BooksServiceException e) {
                System.out.println(e.getFaultInfo().getMessage());
            } catch (UnuathorizedException e) {
                System.out.println(e.getFaultInfo().getMessage());
                makeAuth();
            } catch (ForbiddenException e) {
                System.out.println(e.getFaultInfo().getMessage());
                makeAuth();
            }
        } else {
            System.out.println("Ничего не введено");
        }
    }

    private static String printBook(BooksEntity b) {
        Formatter fmt = new Formatter();
        return fmt.format("ID: %d, Book: %s, Author: %s, PublicDate: %s, ISBN: %s", b.getId(), b.getName(), b.getAuthor(), b.getPublicDate().toString(), b.getIsbn()).toString();
    }

    private static String checkEmpty(String s) {
        return s.length() == 0 ? null : s;
    }
}
