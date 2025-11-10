package server.datasource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import server.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class UserDataSource {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger log = LoggerFactory.getLogger(UserDataSource.class);
    private final String USERS_FILE = "data/users.json";

    public List<User> loadUsers() {
        try {
            Gson gson = new Gson();
            Reader reader = new FileReader(USERS_FILE);

            Type userListType = new TypeToken<Map<String, List<User>>>() {}.getType();
            Map<String, List<User>> userMap = gson.fromJson(reader, userListType);

            List<User> users = userMap.get("users");
            log.info("UserDataSource: Đã tải {} người dùng.", users.size());
            return users;
        } catch (FileNotFoundException e) {
            log.error("Không tìm thấy file {}. Trả về danh sách rỗng.", USERS_FILE, e);
            return Collections.emptyList();
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
