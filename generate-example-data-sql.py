################################################################################
# Class: CS 1555 Database Management Systems
# Instructor: Prof. Mohamed Sharaf
# Contributors:
#   - Benjamin Lind (bdl22)
#   - Autumn Good (alg161)
#   - Fadi Alchoufete (fba4)
#
# generate-example-data-sql.py generates random SQL INSERT statements to
# populate the database tables with test data.
################################################################################

#!/usr/bin/python
import sys
import random
import datetime

def main():
    sql = list()

    num_users       = 100  # number of users to generate
    num_friendships = 200  # number of groups to generate
    num_groups      = 10   # number of groups to generate
    num_messages    = 300  # number of groups to generate
    
    user_sql = random_user_sql(num_users)
    sql += user_sql

    friendship_sql = random_friendship_sql(num_friendships, num_users)
    sql += friendship_sql

    group_sql = random_group_sql(num_groups)
    sql += group_sql

    group_member_sql = random_group_membership_sql(100, num_groups, num_users)
    sql += group_member_sql

    message_sql = random_message_sql(num_messages, num_users)
    sql += message_sql

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
            email = fname.lower() + lname.lower() + str(email_num) \
                + "@test-domain.com"
            email_num += 1
        emails.append(email)

        sql.append("INSERT INTO FS_User (name, email, dob, last_login) "
                   "VALUES ('%s', '%s', TO_DATE('%s', 'YYYY-MM-DD'), "
                   "TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS'));"
                   % (fname + " " + lname, email, dob, login))

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
            est_date_raw = random_date_between(est_range_start,
                                               est_range_end, False)
            est_date = "TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS')" % est_date_raw

        friend_initiator = None
        friend_receiver  = None
        found_empty_friendship = False
        while not found_empty_friendship:
            friend_initiator = random.choice(range(1, num_users))
            friend_receiver = random.choice(range(1, num_users))
            if (friend_initiator != friend_receiver and
                friendships[friend_initiator][friend_receiver] == False):
                found_empty_friendship = True
                friendships[friend_initiator][friend_receiver] = True
                friendships[friend_receiver][friend_initiator] = True
            
        sql.append("INSERT INTO Friendship (friend_initiator, "
                   "friend_receiver, established, date_established) "
                   "VALUES (%d, %d, %d, %s);"
                   % (friend_initiator, friend_receiver, established, est_date))

    sql.append("")
    return sql



def random_message_sql(num_to_generate, num_users):
    subject_pool = [
        "Lunch", "Dinner", "Summer Vacation", "Sushi", "Funny Video", "Lottery",
        "What Stacy Said", "SNL"
    ]

    message_pool = [
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        "Duis turpis lectus, luctus sodales pretium sit amet, malesuada eget diam.",
        "Donec congue purus facilisis posuere rutrum. Vivamus condimentum lacus.",
        "Vitae nisi molestie vestibulum. Curabitur nec libero eu ipsum sollicitudin.",
        "Consequat id id risus. Nulla facilisi. Sed iaculis, risus a volutpat.",
        "Rutrum, urna eros sagittis metus, ac fringilla nunc metus non dolor. Sed.",
        "Cursus tortor et rhoncus scelerisque. Ut imperdiet rutrum magna, a.",
        "Pellentesque odio consequat eget. Aliquam varius, nibh eget maximus.",
        "Placerat, quam eros congue lectus, eget fringilla lorem enim in erat.",
        "Maecenas id turpis felis. Nulla ipsum odio, vulputate at cursus venenatis.",
        "Pretium non tellus. Fusce ac magna vulputate, varius nisi eget, dapibus.",
        "Mauris. Vestibulum sed dolor sit amet eros tempor vehicula. Sed placerat.",
        "Eleifend nisi vel viverra. Aenean tristique iaculis nisl. Sed fermentum.",
        "Turpis a tincidunt posuere."
    ]

    message_date_range_start = datetime.datetime.strptime('2015-01-01 00:00:00',
                                                 '%Y-%m-%d %H:%M:%S')
    message_date_range_end   = datetime.datetime.strptime('2016-04-01 00:00:00',
                                                 '%Y-%m-%d %H:%M:%S')

    sql = ['--------------------------------------------',
           '----------- GENERATING MESSAGES ------------',
           '--------------------------------------------']

    for i in range(0, num_to_generate):
        message_date_raw = random_date_between(message_date_range_start,
                                               message_date_range_end, False)
        message_date_sent = "TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS')" \
                            % message_date_raw

        message_sender = random.choice(range(1, num_users))
        message_recipient = message_sender
        while message_sender == message_recipient:
            message_recipient = random.choice(range(1, num_users))

        message_subject = random.choice(subject_pool)
        message_body    = random.choice(message_pool)[:100]  # max 100 characters in column

        sql.append("INSERT INTO Message (subject, body, recipient, sender, date_sent) "
                   "VALUES ('%s', '%s', %d, %d, %s);"
                   % (message_subject, message_body, message_recipient,
                      message_sender, message_date_sent))

    return sql



def random_date_between(start, end, date_only):
    diff = end - start
    int_diff = (diff.days * 24 * 60 * 60) + diff.seconds
    random_second = random.randrange(int_diff)
    final_date = start + datetime.timedelta(seconds=random_second)
    
    if (date_only):
        return final_date.strftime('%Y-%m-%d')
    
    return final_date.strftime('%Y-%m-%d %H:%M:%S')

def random_group_sql(num_to_generate):

    sql = ['--------------------------------------------',
           '------------- GENERATING GROUPS ------------',
           '--------------------------------------------']
    group_names_pool = [
        "Book Club", "Swim Team", "Bake Club", "Bird Watchers", "Summer Camp", "Knitting Circle"
        ]
    group_descriptions_pool = [
        "For fellow enthusiasts", "To share events and outings", "For serious fans only", "Keep up with updates and events"
        ]

    for i in range(0, num_to_generate):
        group_name = random.choice(group_names_pool)
        group_description = random.choice(group_descriptions_pool)
        enroll_limit = random.randint(75, 100)
        sql.append("INSERT INTO User_Group (group_name, group_description, group_enroll_limit) "
                   "VALUES ('%s', '%s',  %i);"
                   % (group_name, group_description, enroll_limit))

    sql.append("")
    return sql

def random_group_membership_sql (num_to_generate, num_groups, num_users):
    sql = ['--------------------------------------------',
           '------- GENERATING GROUP MEMBERSHIP --------',
           '--------------------------------------------']
    
    group_members = [[] for x in range(num_groups)]

    for i in range(0, num_to_generate):
        rand_group_id = random.randint(1,num_groups)
        rand_user_id = random.randint(1,num_users)

        while rand_user_id in group_members[rand_group_id-1]:
            rand_group_id = random.randint(1,num_groups)
            rand_user_id = random.randint(1,num_users)

        group_members[rand_group_id-1].append(rand_user_id)
        sql.append("INSERT INTO Group_Member (group_id, user_id) "
                   "VALUES (%i, %i);"
                   % (rand_group_id, rand_user_id))

    sql.append("")
    return sql
        
    


        
if __name__ == '__main__':
    main()
