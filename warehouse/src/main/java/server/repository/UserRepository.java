package server.repository;

import server.datasource.UserDataSource;
import server.model.User;

import java.util.ArrayList; // Thêm import này
import java.util.LinkedHashMap; // Thêm import này
import java.util.List;
import java.util.Map; // Thêm import này
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserRepository {

    private final Map<String, User> userMap;
    private final UserDataSource dataSource;

    public UserRepository(UserDataSource dataSource) {
        this.dataSource = dataSource;

        this.userMap = new LinkedHashMap<>();
        List<User> userList = dataSource.loadUsers();
        for (User u : userList) {
            this.userMap.put(u.getUsername(), u);
        }
    }

    public List<User> findAll() {
        return new ArrayList<>(userMap.values());
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();

        return Optional.ofNullable(userMap.get(username));
    }

    public boolean existsByUsername(String username) {
        return userMap.containsKey(username);
    }

    public boolean save(User user) {
        userMap.put(user.getUsername(), user);
        return persist();
    }

    public boolean update(User user) {
        userMap.put(user.getUsername(), user);
        return persist();
    }

    private boolean persist() {
        try {
            dataSource.saveUsers(new ArrayList<>(userMap.values()));
            System.out.println("Đang lưu thay đổi vào users.json...");
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public List<User> getPaginatedUsers(String searchTerm, Boolean isActive, int page, int pageSize) {

        Stream<User> stream = userMap.values().stream();

        if (isActive != null) {
            stream = stream.filter(u -> u.isActive() == isActive);
        }

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearch = searchTerm.toLowerCase();
            stream = stream.filter(u ->
                    u.getUsername().toLowerCase().contains(lowerSearch) ||
                            (u.getFullName() != null && u.getFullName().toLowerCase().contains(lowerSearch))
            );
        }

        return stream
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public long countUsers(String searchTerm, Boolean isActive) {
        Stream<User> stream = userMap.values().stream();

        if (isActive != null) {
            stream = stream.filter(u -> u.isActive() == isActive);
        }

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearch = searchTerm.toLowerCase();
            stream = stream.filter(u ->
                    u.getUsername().toLowerCase().contains(lowerSearch) ||
                            (u.getFullName() != null && u.getFullName().toLowerCase().contains(lowerSearch))
            );
        }
        return stream.count();
    }
}