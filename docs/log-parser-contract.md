## Log Line Grammar

```
LogLine := LeadingSeparator?
    + Timestamp
    + Separator
    + Level
    + Separator
    + ServiceName
    + MessagePart?
    
LeadingSeparator = Separator

MessagePart = Separator + Message?

<Separator> := One or More characters from {" ", "\t"}
```


----

## Quy trình chuyển đổi Log Line:
```
Raw log line
    |
Bỏ qua leading separator
    |
Xác định Timestamp -> parse về Instant
    |
Bỏ qua separator
    |
Xác định Level -> Uppercase -> parse về LogLevel
    |
Bỏ qua separator
    |
Xác định ServiceName
    |
Bỏ qua separator
    |
Xác định Message. 
    -> Nếu không có, đặt Message = ""
    -> Nếu có (Tồn tại ký tự khác Separator) -> Preserve toàn bộ trong Mesage
    |
Trả về LogEntry với các thành phần đã được xác định
```

----

## Kịch bản cần test

### Thành công

1. **Normal Input**

Với Log Line có đầy đủ các thành phần cơ bản và đẹp như:
`2026-08-16T10:15:30Z INFO auth-service message`
- Tạo LogEntry với các giá trị tương ứng

2. **Leading/multiple structural separator**

Với Log Line chứa đầy đủ các thành phần và có Leading Separator như `"     LogLine"`, `"  \tLogLine"`
- Tạo LogEntry với các nội dung xác định

3. **Mixed-case Level**

Với Log Line có Level như "inFo", "Info" hoặc "info"
- Tạo LogEntry với các nội dung xác định và `level = LogLevel.INFO`

4. **No message**

Với Log Line có đủ 3 thành phần đã xác định thành công và không chứa `Separator` sau `ServiceName`
- Tạo LogEntry với nội dung của 3 thành phần đã xác định và `message = ""`

5. **Separator after service but no message**

Với Log Line có đủ 3 thành phần đã xác định thành công và chứa `Separator` sau `ServiceName`
- Tạo LogEntry với nội dung của 3 thành phần đã xác định và `message = ""`

6. **Message contains internal/trailing whitespace**

Với Log Line có đủ 3 thành phần đã xác định thành công và sau ServiceName có nhiều Separator và có Message
-  Tạo LogEntry với nội dung của 3 thành phần đã xác định. Trong `message` không chứa `Leading Separator` và nếu trong `message` có chứa ký tự là `Separator` ở giữa hoặc cuối thì phải đước preserve

### Exception

Ta sẽ có 2 trường hợp tổng quát:
- Raw input là `null` -> `NullPointerException`
- Nếu là malformed -> `LogLineParseException`

Ta sẽ đặt `LogLineParseException` là unchecked. Trong `LogLineParseException` ta sẽ giữ 2 thông tin quan trọng:
- cause?: original exception
- reason: `ParseFailureReason` với các trường hợp
    - `BLANK_LINE`: Nếu Log Line chỉ toàn `Separator`
    - `INVALID_TIMESTAMP`: Nếu lỗi parse `Timestamp`
    - `MISSING_LEVEL`: Nếu thiếu Level
    - `INVALID_LEVEL`: Nếu sau khi normalize Level và lỗi parse sang LogLevel
    - `MISSING_SERVICE_NAME`: Nếu thiếu Service Name


    