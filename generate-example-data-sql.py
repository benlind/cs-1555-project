#!/usr/bin/python
import sys
import random
import datetime

def main():
    sql = list()

    num_users = 100  # number of users to generate
    
    user_sql = random_user_sql(num_users)
    sql += user_sql

    friendship_sql = random_friendship_sql(200, num_users)
    sql += friendship_sql

    final_sql = "\n".join(sql)

    sql_file = open('add-test-data.sql', 'w')
    print(final_sql)
    sql_file.write(final_sql)

    
    
def random_user_sql(num_to_generate):
    first_names_pool = [
        "John", "Mary", "Tim", "Alice", "Ben", "Jane", "Tom", "Emily"
    ]
    last_names_pool = [
        "Doe", "Cooper", "Brown", "James", "Johnson", "Smith", "Fields"
    ]
    sql = ['--------------------------------------',
           '---------- GENERATING USERS ----------',
           '--------------------------------------']
    dob_range_start = datetime.datetime.strptime('1940-01-01 00:00:00',
                                                 '%Y-%m-%d %H:%M:%S')
    dob_range_end   = datetime.datetime.strptime('2000-01-01 00:00:00',
                                                 '%Y-%m-%d %H:%M:%S')
    login_range_start = datetime.datetime.strptime('2015-01-01 00:00:00',
                                                   '%Y-%m-%d %H:%M:%S')
    login_range_end   = datetime.datetime.strptime('2016-04-01 00:00:00',
                                                   '%Y-%m-%d %H:%M:%S')
    emails = []

    for i in range(0, num_to_generate):
        fname = random.choice(first_names_pool)
        lname = random.choice(last_names_pool)
        dob = random_date_between(dob_range_start, dob_range_end, True)
        login = random_date_between(login_range_start, login_range_end, False)

        found_email = False
        email_num = 2
        email = fname.lower() + lname.lower() + "@test-domain.com"
        while email in emails:
            email = fname.lower() + lname.lower() + str(email_num) + "@test-domain.com"
            email_num += 1
        emails.append(email)

        sql.append("INSERT INTO Users (user_id, name, email, dob, last_login) "
                   "VALUES (%d, '%s', '%s', TO_DATE('%s', 'YYYY-MM-DD'), "
                   "TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS'));"
                   % (i, fname + " " + lname, email, dob, login))

    sql.append("")
    return sql

def random_friendship_sql(num_to_generate, num_users):
    sql = ['--------------------------------------------',
           '---------- GENERATING FRIENDSHIPS ----------',
           '--------------------------------------------']
    est_range_start = datetime.datetime.strptime('2015-01-01 00:00:00',
                                                 '%Y-%m-%d %H:%M:%S')
    est_range_end   = datetime.datetime.strptime('2016-04-01 00:00:00',
                                                 '%Y-%m-%d %H:%M:%S')

    # 2D list to ensure there are no friendship duplicates
    friendships = [[False for x in range(num_users)] for x in range(num_users)] 
    
    for i in range(0, num_to_generate):
        established = random.choice([1, 0])
        est_date = "NULL"
        if (established):
            est_date_raw = random_date_between(est_range_start, est_range_end, False)
            est_date = "TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS')" % est_date_raw

        friend_initiator = None
        friend_receiver  = None
        found_empty_friendship = False
        while not found_empty_friendship:
            friend_initiator = random.choice(range(0, num_users))
            friend_receiver = random.choice(range(0, num_users))
            if (friendships[friend_initiator][friend_receiver] == False):
                found_empty_friendship = True
                friendships[friend_initiator][friend_receiver] = True
                friendships[friend_receiver][friend_initiator] = True
            
        sql.append("INSERT INTO Friendships (friendship_id, friend_initiator, "
                   "friend_receiver, established, date_established) "
                   "VALUES (%d, %d, %d, %d, %s);"
                   % (i, friend_initiator, friend_receiver, established, est_date))

    sql.append("")
    return sql

def random_date_between(start, end, date_only):
    diff = end - start
    int_diff = (diff.days * 24 * 60 * 60) + diff.seconds
    random_second = random.randrange(int_diff)
    final_date = start + datetime.timedelta(seconds=random_second)
    
    if (date_only):
        return final_date.strftime('%Y-%m-%d')
    
    return final_date.strftime('%Y-%m-%d %H:%M:%S')


        
if __name__ == '__main__':
    main()
