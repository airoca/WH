const express = require('express');
const mysql = require('mysql');
const bodyParser = require('body-parser');

const app = express();
const port = 3000;

// JSON 요청 본문을 구문 분석하기 위한 body-parser 미들웨어 사용 (크기 제한을 늘림)
app.use(bodyParser.json({ limit: '50mb' }));
app.use(bodyParser.urlencoded({ limit: '50mb', extended: true }));

// MySQL 연결 설정
const connection = mysql.createConnection({
  host: 'slang-database.chyq0kgmmn8p.ap-northeast-2.rds.amazonaws.com',
  user: 'yh0872',
  password: 'rj7783208232',
  database: 'wash',
  port: 3306
})

// 데이터베이스 연결
connection.connect();

// 전체 사용자 조회
app.get('/users', (req, res) => {
  connection.query('SELECT * FROM user', (error, results, fields) => {
    if (error) throw error;
    res.json(results);
  });
});

const bcrypt = require('bcrypt');
const saltRounds = 10; // salt 라운드 수 설정

//회원가입
app.post('/register', (req, res) => {
  const { userid, pw, username, dormitory, gender, image } = req.body;
  console.log('회원가입 요청:', req.body);

  // 비밀번호 해시 생성
  bcrypt.hash(pw, saltRounds, function(err, hash) {
    if (err) {
      console.error(err);
      return res.status(500).send('Error while hashing password');
    }

    connection.query('SELECT * FROM user WHERE userid=?', [userid], (err, data) => {
      if (data.length == 0) {
        console.log('회원가입 성공');
        // 모든 사용자 정보를 데이터베이스에 저장
        connection.query('INSERT INTO user(userid, password, username, dormitory, gender, image) VALUES(?,?,?,?,?,?)', 
                         [userid, hash, username, dormitory, gender, image]);
        res.status(200).json({ "message": true });
      } else {
        console.log('회원가입 실패 - 이미 존재하는 사용자');
        res.status(200).json({ "message": false });
      }
    });
  });
});

//ID 중복 체크
app.post('/checkUserId', (req, res) => {
  const { userid } = req.body;
  console.log('ID 중복 확인 요청:', userid);

  connection.query('SELECT * FROM user WHERE userid=?', [userid], (err, data) => {
    if (err) {
      console.error(err);
      return res.status(500).send('Database query error');
    }

    if (data.length > 0) {
      // 동일한 userid가 존재하는 경우
      res.status(200).json({ "isAvailable": false });
    } else {
      // 사용 가능한 userid
      res.status(200).json({ "isAvailable": true });
    }
  });
});

// 로그인
app.post('/login', (req, res) => {
  const { userid, pw } = req.body;
  console.log('로그인 요청:', req.body);

  connection.query('SELECT * FROM user WHERE userid=?', [userid], (err, data) => {
    if (err) {
      console.error(err);
      return res.status(500).send('Database query error');
    }

    if (data.length > 0) {
      // 데이터베이스에서 저장된 해시와 사용자의 비밀번호 비교
      bcrypt.compare(pw, data[0].password, function(err, result) {
        if (result) {
          console.log('로그인 성공');
          res.status(200).json({
            "message": true,
            "UID": data[0].UID
          });
        } else {
          console.log('로그인 실패');
          res.status(200).json({
            "message": false,
            "UID": -1
          });
        }
      });
    } else {
      console.log('해당 사용자 없음');
      res.status(200).json({
        "message": false,
        "UID": -1
      });
    }
  });
});

// 사용자 정보 조회
app.get('/userdetails/:userid', (req, res) => {
  const userid = req.params.userid;

  connection.query('SELECT userid, password, username, dormitory, gender, image FROM user WHERE userid = ?', [userid], (error, results) => {
    if (error) {
      console.error(error);
      return res.status(500).send('Database query error');
    }

    if (results.length > 0) {
      // 여기서 image 필드를 문자열로 처리
      let user = results[0];
      user.image = user.image ? user.image.toString() : null; // image 필드가 BLOB 또는 비슷한 타입인 경우
      res.status(200).json(user);
    } else {
      res.status(404).send('User not found');
    }
  });
});

// 서버 시작
app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}/`);
})

///////////////////////////////////////////////////////////////////////////////////////////////////////////

// 모든 세탁기 조회
app.get('/washers', (req, res) => {
  connection.query('SELECT * FROM washer', (error, results, fields) => {
    if (error) throw error;
    res.json(results);
  });
});
// 사랑관에 있는 세탁기만 조회
app.get('/washers1', (req, res) => {
  connection.query('SELECT * FROM washer WHERE dorm = "사랑관"', (error, results, fields) => {
    if (error) throw error;
    res.json(results);
  });
});
// 소망관에 있는 세탁기만 조회
app.get('/washers2', (req, res) => {
  connection.query('SELECT * FROM washer WHERE dorm = "소망관"', (error, results, fields) => {
    if (error) throw error;
    res.json(results);
  });
});
// 아름관에 있는 세탁기만 조회
app.get('/washers3', (req, res) => {
  connection.query('SELECT * FROM washer WHERE dorm = "아름관"', (error, results, fields) => {
    if (error) throw error;
    res.json(results);
  });
});
// 나래관에 있는 세탁기만 조회
app.get('/washers4', (req, res) => {
  connection.query('SELECT * FROM washer WHERE dorm = "나래관"', (error, results, fields) => {
    if (error) throw error;
    res.json(results);
  });
});

// 세탁기 상태 조회 및 업데이트
app.post('/washerstatus/:id', (req, res) => {
  const id = req.params.id;
  const { starttime, settime, userid } = req.body; // 사용자 ID 추가

  console.log(`Received request to update washer status: id=${id}, starttime=${starttime}, settime=${settime}, userid=${userid}`);

  // 세탁기 상태를 '사용중'으로 업데이트
  connection.query('UPDATE washer SET starttime = ?, settime = ?, userid = ?, washerstatus = "사용중" WHERE id = ?', 
  [starttime, settime, userid, id], (updateError, updateResults) => {
    if (updateError) {
      console.log(updateError);
      res.status(500).json({ "message": "Internal Server Error" });
      return;
    }

    // WasherUsage에 사용 기록 추가

    // 업데이트된 세탁기 상태 조회
    connection.query('SELECT washerstatus FROM washer WHERE id = ?', [id], (selectError, selectResults) => {
      if (selectError) {
        console.log(selectError);
        res.status(500).json({ "message": "Internal Server Error" });
      } else if (selectResults.length === 0) {
        console.log('해당 ID를 가진 세탁기가 없음');
        res.status(404).json({ "message": "Washer Not Found" });
      } else {
        console.log('세탁기 상태 조회 성공');
        res.json({ "washerstatus": selectResults[0].washerstatus });
      }
    });
  });
});

// 세탁기 사용 종료 및 상태 업데이트
app.post('/washerend/:id', (req, res) => {
  const washerId = req.params.id;

  // 먼저 reservation 테이블에서 해당 washerId를 확인
  connection.query('SELECT * FROM reservation_washer WHERE washer_id = ?', [washerId], (selectError, selectResults) => {
    if (selectError) {
      console.error(selectError);
      res.status(500).json({ message: false });
      return;
    }

    let newStatus;
    if (selectResults.length > 0) {
      // 예약이 존재하는 경우, 세탁기 상태를 '예약중'으로 변경
      newStatus = "예약중";
    } else {
      // 예약이 존재하지 않는 경우, 세탁기 상태를 '사용 가능'으로 변경
      newStatus = "사용 가능";
    }

    // 세탁기 상태 및 starttime, settime, userid 업데이트
    connection.query(
      'UPDATE washer SET washerstatus = ?, starttime = NULL, settime = NULL, userid = NULL WHERE id = ?', 
      [newStatus, washerId], 
      (updateError) => {
        if (updateError) {
          console.error(updateError);
          res.status(500).json({ message: false });
        } else {
          res.status(200).json({ message: selectResults.length === 0 });
        }
      }
    );
  });
});

app.post('/cancelWasherReservation', (req, res) => {
  const { userid } = req.body;

  // 먼저 해당 userid의 현재 washer_id를 찾음
  connection.query('SELECT washer_id FROM reservation_washer WHERE userid = ?', [userid], (error, results) => {
    if (error) {
      console.error(error);
      return res.status(500).json({ message: false });
    }

    if (results.length === 0) {
      console.log('해당 userid에 대한 예약이 없음');
      return res.status(200).json({ message: true });
    }

    const washerId = results[0].washer_id;

    // reservation_washer 테이블에서 washer_id를 NULL로 업데이트
    connection.query('UPDATE reservation_washer SET washer_id = NULL WHERE userid = ?', [userid], (error, updateResults) => {
      if (error) {
        console.error(error);
        return res.status(500).json({ message: false });
      }

      // reservation_washer 테이블에서 해당 washer_id의 존재 여부를 확인
      connection.query('SELECT * FROM reservation_washer WHERE washer_id = ?', [washerId], (error, reservationWasherResults) => {
        if (error) {
          console.error(error);
          return res.status(500).json({ message: false });
        }

        // 해당 washer_id가 reservation_washer 테이블에 존재하지 않는 경우에만 washerstatus 업데이트
        if (reservationWasherResults.length === 0) {
          connection.query('UPDATE washer SET washerstatus = "사용 가능" WHERE id = ? AND washerstatus = "예약중"', [washerId], (error, washerUpdateResults) => {
            if (error) {
              console.error(error);
              return res.status(500).json({ message: false });
            }

            console.log('세탁기 예약 취소 및 상태 업데이트 성공');
            res.status(200).json({ message: true });
          });
        } else {
          console.log('다른 예약이 존재하여 세탁기 상태 업데이트를 수행하지 않음');
          res.status(200).json({ message: true });
        }
      });
    });
  });
});

// 특정 세탁기를 예약한 모든 사람들의 username 반환
app.get('/washerReservations/:washerId', (req, res) => {
  const washerId = req.params.washerId;

  const query = `
    SELECT u.username 
    FROM user u 
    JOIN reservation_washer r ON u.userid = r.userid 
    WHERE r.washer_id = ?
    ORDER BY r.reservation_washer_time ASC
  `;

  connection.query(query, [washerId], (error, results) => {
    if (error) {
      console.error(error);
      return res.status(500).send('Database query error');
    }

    res.json(results.map(row => row.username));
  });
});

// 특정 세탁기 예약하기
app.post('/reserveWasher', (req, res) => {
  const { userid, washerId } = req.body;

  // 해당 userid의 기존 예약을 확인
  connection.query('SELECT * FROM reservation_washer WHERE userid = ?', [userid], (selectError, selectResults) => {
      if (selectError) {
          console.error(selectError);
          return res.status(500).json({ message: false });
      }

      if (selectResults.length > 0) {
          // userid가 존재하는 경우
          const existingReservation = selectResults[0];
          if (existingReservation.washer_id === null) {
              // washer_id가 null인 경우, washer_id를 업데이트
              connection.query('UPDATE reservation_washer SET washer_id = ? WHERE userid = ?',
                  [washerId, userid],
                  (updateError, updateResults) => {
                      if (updateError) {
                          console.error(updateError);
                          return res.status(500).json({ message: false });
                      }
                      // 예약 업데이트 성공
                      res.status(200).json({ message: true });
                  }
              );
          } else {
              // 이미 유효한 예약이 존재하는 경우
              return res.status(200).json({ message: false });
          }
      } else {
          // userid가 존재하지 않는 경우, 새로운 예약 추가
          connection.query('INSERT INTO reservation_washer (userid, washer_id) VALUES (?, ?)', 
              [userid, washerId], 
              (insertError, insertResults) => {
                  if (insertError) {
                      console.error(insertError);
                      return res.status(500).json({ message: false });
                  }
                  // 새 예약 성공적으로 추가
                  res.status(200).json({ message: true });
              }
          );
      }
  });
});

// 특정 사용자의 모든 세탁기 조회
app.get('/washersByUser/:userid', (req, res) => {
  const userid = req.params.userid;

  connection.query('SELECT * FROM washer WHERE userid = ?', [userid], (error, results) => {
    if (error) {
      console.error(error);
      return res.status(500).send('Database query error');
    }

    res.json(results);
  });
});

// 사용자 정보 업데이트
app.post('/updateUser', (req, res) => {
  const { userid, currentPassword, newPassword, username, dormitory, gender, image } = req.body;

  // 먼저 사용자의 현재 비밀번호 확인
  connection.query('SELECT password FROM user WHERE userid = ?', [userid], (error, results) => {
    if (error) {
      console.error(error);
      return res.status(500).send('Database query error');
    }

    if (results.length == 0) {
      return res.status(404).send('User not found');
    }

    const hashedPassword = results[0].password;

    // 비밀번호 비교
    bcrypt.compare(currentPassword, hashedPassword, function(err, isMatch) {
      if (err) {
        console.error(err);
        return res.status(500).send('Error while comparing password');
      }

      if (!isMatch) {
        return res.status(401).send('Password does not match');
      }

      // 새로운 비밀번호를 해시하고, 사용자 정보 업데이트
      bcrypt.hash(newPassword, saltRounds, function(err, newHash) {
        if (err) {
          console.error(err);
          return res.status(500).send('Error while hashing new password');
        }

        connection.query('UPDATE user SET password = ?, username = ?, dormitory = ?, gender = ?, image = ? WHERE userid = ?', 
                         [newHash, username, dormitory, gender, image, userid], (updateError) => {
          if (updateError) {
            console.error(updateError);
            return res.status(500).send('Error while updating user information');
          }

          res.status(200).json({ "message": true });
        });
      });
    });
  });
});

// 사용자가 예약한 세탁기의 모든 정보 조회
app.get('/washerReservationsByUser/:userid', (req, res) => {
  const userid = req.params.userid;

  const query = `
    SELECT w.* 
    FROM washer w 
    JOIN reservation_washer rw ON w.id = rw.washer_id 
    WHERE rw.userid = ?
  `;

  connection.query(query, [userid], (error, results) => {
    if (error) {
      console.error(error);
      return res.status(500).send('Database query error');
    }

    res.json(results);
  });
});

// 특정 세탁기의 washer_id에 해당하는 모든 username 조회
app.get('/usernamesByWasher/:washerId', (req, res) => {
  const washerId = req.params.washerId;

  const query = `
    SELECT u.username 
    FROM user u
    JOIN reservation_washer rw ON u.userid = rw.userid
    WHERE rw.washer_id = ?
    ORDER BY rw.reservation_washer_time ASC
  `;

  connection.query(query, [washerId], (error, results) => {
    if (error) {
      console.error(error);
      return res.status(500).send('Database query error');
    }

    // 결과를 username 배열로 변환
    const usernames = results.map(row => row.username);
    res.json(usernames);
  });
});


// 모든 세탁기의 상태 초기화 및 reservation_washer 테이블 내용 삭제
app.post('/resetWashers', (req, res) => {
  // 트랜잭션 시작
  connection.beginTransaction(err => {
    if (err) {
      console.error('Failed to start transaction', err);
      return res.status(500).json({ message: "Failed to start transaction" });
    }

    // reservation_washer 테이블 내용 삭제
    const deleteReservationsQuery = 'DELETE FROM reservation_washer';

    connection.query(deleteReservationsQuery, (deleteError, deleteResults) => {
      if (deleteError) {
        return connection.rollback(() => {
          console.error('Failed to delete reservations', deleteError);
          res.status(500).json({ message: "Failed to delete reservations" });
        });
      }

      // washer 테이블 업데이트
      const resetWashersQuery = `
        UPDATE washer
        SET washerstatus = "사용 가능", starttime = NULL, settime = NULL, userid = NULL
      `;

      connection.query(resetWashersQuery, (resetError, resetResults) => {
        if (resetError) {
          return connection.rollback(() => {
            console.error('Failed to reset washers', resetError);
            res.status(500).json({ message: "Failed to reset washers" });
          });
        }

        // 트랜잭션 커밋
        connection.commit(commitError => {
          if (commitError) {
            return connection.rollback(() => {
              console.error('Failed to commit transaction', commitError);
              res.status(500).json({ message: "Failed to commit transaction" });
            });
          }

          console.log('All washers reset successfully and reservations deleted');
          res.status(200).json({ message: true });
        });
      });
    });
  });
});