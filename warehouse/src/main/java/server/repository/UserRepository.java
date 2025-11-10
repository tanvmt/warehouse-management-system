package server.repository;

import server.datasource.UserDataSource;
import server.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserRepository {

    private final List<User> users;
    private final UserDataSource dataSource;

    public UserRepository(UserDataSource dataSource) {
        this.dataSource = dataSource;
        this.users = dataSource.loadUsers(); // Load 1 lần lúc khởi động
    }

    public List<User> findAll() {
        return users;
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    public boolean existsByUsername(String username) {
        return users.stream().anyMatch(u -> u.getUsername().equals(username));
    }

    public boolean save(User user) {
        // Thêm user mới vào danh sách (đã check trùng lặp ở service)
        users.add(user);
        return persist(); // Lưu thay đổi ra file
    }

    public boolean update(User user) {
        // (Logic tìm và thay thế user trong list)
        // Vì 'user' là một object đã được lấy từ list,
        // việc thay đổi nó (ví dụ user.setActive(true))
        // sẽ thay đổi trực tiếp. Chúng ta chỉ cần persist.
        return persist();
    }

    private synchronized boolean persist() {
        // Đây là ví dụ, bạn cần hàm saveUsers trong dataSource
        // return dataSource.saveUsers(users);
        System.out.println("Đang lưu thay đổi vào users.json...");
        return true; // Giả định là lưu thành công
    }

    // Logic Phân trang / Lọc / Tìm kiếm MỚI
    public List<User> getPaginatedUsers(String searchTerm, Boolean isActive, int page, int pageSize) {
        Stream<User> stream = users.stream();

        // 1. Filter
        if (isActive != null) {
            stream = stream.filter(u -> u.isActive() == isActive);
        }

        // 2. Search
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearch = searchTerm.toLowerCase();
            stream = stream.filter(u ->
                    u.getUsername().toLowerCase().contains(lowerSearch) ||
                            (u.getFullName() != null && u.getFullName().toLowerCase().contains(lowerSearch))
            );
        }

        // 3. Paginate
        return stream
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    // Lấy tổng số lượng để phân trang
    public long countUsers(String searchTerm, Boolean isActive) {
        Stream<User> stream = users.stream();

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