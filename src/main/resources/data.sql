-- users
INSERT INTO users (id, email, password, role) VALUES
                                                  (1, 'test@example.com',  'password123', 'USER'),
                                                  (2, 'owner@example.com', 'password456', 'MERCHANT'),
                                                  (3, 'owner2@example.com','password789', 'MERCHANT');  -- 추가

-- stores  (user_id 2, 3로 분리)
INSERT INTO stores (id, name, address, category, intro, number, start_time, end_time, thumbnail, like_count, user_id) VALUES
                                                                                                                          (1, '테스트 카페', '서울시 강남구 테헤란로 123', 'CAFE', '맛있는 커피를 제공하는 카페입니다',
                                                                                                                           '02-1234-5678', '08:00:00', '22:00:00', 'cafe_thumbnail.jpg', 10, 2),
                                                                                                                          (2, '팝업 스토어', '서울시 홍대입구역 근처', 'SHOPPING', '트렌디한 쇼핑 아이템',
                                                                                                                           '02-8765-4321', '11:00:00', '21:00:00', 'fashion_thumbnail.jpg', 25, 3);

-- Events (store_id=1을 명시적으로 참조)
INSERT INTO events (
    id, name, description, intro, thumbnail,
    start_date, end_date, start_time, end_time, like_count, store_id
) VALUES
      (1, '현재 진행중 이벤트', '지금 진행중인 이벤트입니다. 다양한 혜택을 누려보세요!', '특별 이벤트', 'event1.jpg',
       DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 10 DAY), '10:00:00', '18:00:00', 45, 1),
      (2, '인기 이벤트', '좋아요가 많은 인기 이벤트입니다', '인기 급상승', 'event2.jpg',
       DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), '11:00:00', '19:00:00', 120, 1),
      (3, '오늘 마감 이벤트', '오늘 끝나는 이벤트, 마지막 기회!', '마지막 기회', 'event3.jpg',
       DATE_SUB(CURDATE(), INTERVAL 3 DAY), CURDATE(), '09:00:00', '22:00:00', 80, 1),
      (4, '예정 이벤트', '곧 시작할 예정인 이벤트입니다', 'Coming Soon', 'event4.jpg',
       DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY), '12:00:00', '20:00:00', 15, 2),
      (5, '또 다른 진행중 이벤트', '현재 진행중인 또 다른 이벤트', '특가 이벤트', 'event5.jpg',
       DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 8 DAY), '13:00:00', '19:00:00', 67, 2);

-- Popups (user_id는 1,2를 명시적으로 참조)
INSERT INTO popups (
    id, name, description, intro, address, category, thumbnail,
    start_date, end_date, start_time, end_time, like_count, user_id
) VALUES (
             1,
             '진행중 팝업 스토어',
             '현재 진행중인 엔터테인먼트 팝업 스토어입니다',
             '엔터테인먼트 전시',
             '서울시 홍대입구 345번지',
             'ENTERTAINMENT',
             'popup1.jpg',
             DATE_SUB(CURDATE(), INTERVAL 1 DAY),
             DATE_ADD(CURDATE(), INTERVAL 7 DAY),
             '10:00:00',
             '20:00:00',
             55,
             1
         );
