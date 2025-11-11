package server.datasource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import server.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;


public class UserDataSource {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger log = LoggerFactory.getLogger(UserDataSource.class);
    private final String USERS_FILE = "data/users.json";

    public List<User> loadUsers() {
        try {
            Reader reader = new FileReader(USERS_FILE);
            Type userListType = new TypeToken<List<User>>() {}.getType();
            List<User> userList = gson.fromJson(reader, userListType);
            log.info("UserDataSource: Đã tải {} người dùng.", userList.size());
            return userList;
        } catch (FileNotFoundException e) {
            log.error("Không tìm thấy file {}. Trả về danh sách rỗng.", USERS_FILE, e);
            return new ArrayList<>();
        }
    }

    public void saveUsers(List<User> users) {
        try (Writer writer = new FileWriter(USERS_FILE)) {
            gson.toJson(users, writer);
            log.info("Luu vao file user thanh cong!!!");
        } catch (Exception e) {
            log.error("Lỗi khi lưu file user: " + e.getMessage());
        }
    }

}
