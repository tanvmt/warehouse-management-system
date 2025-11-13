package server.mapper;

import com.group9.warehouse.grpc.UserProfile;
import server.model.User;

public class UserMapper {

    public  UserProfile convertUserToProfile(User user) {
        UserProfile.Builder builder = UserProfile.newBuilder();
        builder.setUsername(user.getUsername())
                .setRole(user.getRole())
                .setIsActive(user.isActive());

        if (user.getFullName() != null) builder.setFullName(user.getFullName());
        if (user.getEmail() != null) builder.setEmail(user.getEmail());
        if (user.getPhone() != null) builder.setPhone(user.getPhone());
        if (user.getSex() != null) builder.setSex(user.getSex());
        if (user.getDateOfBirth() != null) builder.setDateOfBirth(user.getDateOfBirth());

        return builder.build();
    }
}
