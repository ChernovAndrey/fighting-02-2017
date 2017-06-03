package services;

import objects.HttpStatus;
import objects.User;
import objects.UsersData;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import services.mappers.UserMapper;
import services.mappers.UsersDataMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrey on 21.02.2017.
 */
@Service
@Repository
public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class);
    private JdbcTemplate jdbcTemplate;

    public UserService(javax.sql.DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UsersData register(User user) {
        if (user.getPassword().isEmpty() || user.getLogin().isEmpty()
                || (user.getPassword().length() < 8) || (user.getLogin().matches("[а-яА-ЯёЁ]+"))
                || (user.getLogin().length() < 4)) {
            return null;
        }
        try {
            registerUser(user);
            return new UsersData(user.getLogin(), 1200, 0, 0, 0, 0, 0, 0);
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Transactional
    private void registerUser(User user) {
        jdbcTemplate.update("insert into users (login,password) values (?,?);", user.getLogin(), user.getHashPassword());
        jdbcTemplate.update("INSERT INTO usersData (login,rating) values (?,?);", user.getLogin(),1200);
    }

    @Transactional
    private int updateUsers(User user) {
        int rowNumb = jdbcTemplate.update("update users set login=? where login=?;", user.getNewlogin(), user.getLogin());
        rowNumb += jdbcTemplate.update("update usersdata set login=? where login=?;", user.getNewlogin(), user.getLogin());
        return rowNumb;
    }

    @Transactional
    private boolean checkINputPasAndLog(User user) {
        final String SQL = "SELECT * FROM users WHERE login = ?";
        final User userDB = jdbcTemplate.queryForObject(SQL,
                new Object[]{user.getLogin()}, new UserMapper());
        return userDB != null && user.comparePass.test(userDB.getPassword());
    }

    @Transactional
    private UsersData getUsersData(String login) {
        final String SQL = "SELECT * FROM usersdata WHERE login = ?";
        try {
            final UsersData usersData = jdbcTemplate.queryForObject(SQL, new Object[]{login}, new UsersDataMapper());
            return usersData;
        } catch (DataAccessException e) {
            return null;
        }
    }

    public @Nullable UsersData getUser(String login) {
        final String SQL = "SELECT * FROM users WHERE login = ?";
        try {
            return getUsersData(login);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public @Nullable UsersData login(User user) {
        try {
            if (checkINputPasAndLog(user)) return getUsersData(user.getLogin());

            log.error("Not Found");
            return null;
        } catch (DataAccessException e) {
            log.error("Not Found");
            return null;
        }
    }

    public String update(User newUser) {
        try {
            final Integer rowNum = updateUsers(newUser);
            if (rowNum == 0) {
                log.error("Bad Request");
                return new HttpStatus().getBadRequest();
            }
            return new HttpStatus().getOk();
        } catch (DataAccessException e) {
            log.error("Bad Request");
            return new HttpStatus().getBadRequest();
        }
    }

    public void updateRating(String winner, String looser){
        final Integer ratingW = jdbcTemplate.queryForObject("Select rating from usersData where login=?",
                new Object[]{winner}, Integer.class);
        final Integer ratingL = jdbcTemplate.queryForObject("Select rating from usersData where login=?",
                new Object[]{looser}, Integer.class);
        final Double Ew=1/(1+(Math.pow(10,((ratingL-ratingW)/400))));      //мат ожидание
        final Double El=1/(1+(Math.pow(10,((ratingW-ratingL)/400))));      //мат ожидание
        final Double newRatingW=ratingW+20*(1.0-Ew);
        final Double newRatingL=ratingW+20*(0.0-El);
        jdbcTemplate.update(
                "UPDATE usersData SET (rating,game_count,game_count_win) = (?,game_count+1,game_count_win+1) WHERE login = ?", newRatingW,winner);
        jdbcTemplate.update(
                "UPDATE usersData SET (rating,game_count,game_count_win) = (?,game_count+1,game_count_win) WHERE login = ?", newRatingL,looser);
    }

    public void updateRating(String winner, String looser,String draw) {
        if (draw.equals("draw")) {
            final Integer ratingW = jdbcTemplate.queryForObject("Select rating from usersData where login=?",
                    new Object[]{winner}, Integer.class);
            final Integer ratingL = jdbcTemplate.queryForObject("Select rating from usersData where login=?",
                    new Object[]{looser}, Integer.class);
            final Double Ew = 1 / (1 + (Math.pow(10, ((ratingL - ratingW) / 400))));      //мат ожидание
            final Double El = 1 / (1 + (Math.pow(10, ((ratingW - ratingL) / 400))));      //мат ожидание
            final Double newRatingW = ratingW + 20 * (0.5 - Ew);
            final Double newRatingL = ratingW + 20 * (0.5 - El);
            jdbcTemplate.update(
                    "UPDATE usersData SET (rating,game_count,game_count_win) = (?,game_count+1,game_count_win+1) WHERE login = ?", newRatingW, winner);
            jdbcTemplate.update(
                    "UPDATE usersData SET (rating,game_count,game_count_win) = (?,game_count+1,game_count_win) WHERE login = ?", newRatingL, looser);

        }
    }
    public @Nullable UsersData updateInfo(UsersData usersData) {
        try {
            final int rownum = jdbcTemplate.update(
                    "UPDATE usersData SET rating = rating + ?, game_count = game_count + ?, game_count_win = game_count_win + ?, " +
                            "crystal_purple = crystal_purple + ?, crystal_red = crystal_red + ?, crystal_blue = crystal_blue + ?, crystal_green = crystal_green + ?" +
                            "WHERE login = ?",
                    usersData.getRating(),
                    usersData.getGameCount(),
                    usersData.getGameCountWin(),
                    usersData.getCrystalPurple(),
                    usersData.getCrystalRed(),
                    usersData.getCrystalBlue(),
                    usersData.getCrystalGreen(),
                    usersData.getLogin()
            );
            if (rownum == 0) {
                log.error("Bad Request");
                return null;
            }
            return usersData;
        } catch (DataAccessException e) {
            return null;
        }
    }

    public String changePass(User user) {
        try {
            if (!checkINputPasAndLog(user)) {
                log.error("Not Found");
                return new HttpStatus().getNotFound();
            }
        } catch (DataAccessException e) {
            return new HttpStatus().getNotFound();
        }

        final String SQL = "UPDATE users SET password= ? where login=?";
        try {
            final int rownum = jdbcTemplate.update(
                    SQL, user.getNewHashPassword(), user.getLogin());
            if (rownum == 0) {
                log.error("Bad Request");
                return new HttpStatus().getBadRequest();
            }
        } catch (DataAccessException e) {
            return new HttpStatus().getBadRequest();
        }
        return new HttpStatus().getOk();
    }

    public JSONArray getLeaders() {
        final JSONArray jsonArray = new JSONArray();
        final String SQL = "SELECT login, rating FROM usersdata ORDER BY rating DESC LIMIT 20";
        final List<UsersData> users = jdbcTemplate.query(SQL, (rs, rownumber) -> {
            final UsersData usersData = new UsersData();
            usersData.setLogin(rs.getString("login"));
            usersData.setRating(rs.getInt("rating"));
            return usersData;
        });
        for (UsersData user : users) {
            jsonArray.put(user.getJson());
        }
        return jsonArray;
    }
}