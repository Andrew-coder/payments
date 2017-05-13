package data;

import payments.model.entity.user.RoleType;
import payments.model.entity.user.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public enum UsersData {
    A(1, "Andriy","Iwanyuk","0958016294","andriy","1997-04-25", RoleType.ADMIN),
    B(2, "John","Doe","0631016341","john","1987-01-15",RoleType.USER),
    C(3,"Anna","Svift","0958026341","anna","1991-01-10",RoleType.USER),
    D(4,"Brad","Johnson","0958085183","brad","1995-01-13",RoleType.USER);

    public User user;

    UsersData(long id, String name, String surname, String cellphone, String password, String date, RoleType type){
        Date birthDate = extractDate(date);
        user = new User.Builder()
                    .setId(id)
                    .setName(name)
                    .setSurname(surname)
                    .setCellphone(cellphone)
                    .setPassword(password)
                    .setBirthDate(birthDate)
                    .setRole(type)
                    .build();
    }

    private Date extractDate(String date){
        try{
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date convertedDate = dateFormat.parse(date);
            java.sql.Date sqlDate = new java.sql.Date(convertedDate.getTime());

            return sqlDate;
        }
        catch (ParseException ex){}
        return null;
    }
}