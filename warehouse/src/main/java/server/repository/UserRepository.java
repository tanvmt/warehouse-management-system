package server.repository;

import server.datasource.UserDataSource;
import server.model.Product;
import server.model.User;

import java.io.IOException;
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

    public Optional<User> findByUsername(String username) {
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
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<User> getPaginatedUsers(String searchTerm, Boolean isActive, int page, int pageSize) {

        Stream<User> stream = userMap.values().stream();

        return filterUsers(stream, searchTerm, isActive)
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public long countUsers(String searchTerm, Boolean isActive) {
        Stream<User> stream = userMap.values().stream();
        return filterUsers(stream, searchTerm , isActive).count();
    }

    public int countTotalUsers() {
        return userMap.size();
    }

    private Stream<User> filterUsers(Stream<User> stream, String searchTerm, Boolean isActive) {
        if (isActive != null) {
            stream = stream.filter(p -> p.isActive() == isActive);
        }
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearch = searchTerm.toLowerCase();
            stream = stream.filter(p ->
                    p.getEmail().toLowerCase().contains(lowerSearch) ||
                            p.getFullName().toLowerCase().contains(lowerSearch) ||
                            p.getUsername().toLowerCase().contains(lowerSearch)

            );
        }
        return stream;
    }
}