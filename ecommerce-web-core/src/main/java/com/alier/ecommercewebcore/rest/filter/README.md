# Request and Trace ID Tracking System

This system provides end-to-end request tracking across the application using unique request and trace IDs.

## How it Works

1. **LoggingContextFilter** assigns unique IDs to each incoming HTTP request:
    - Generates UUIDs if no IDs are provided in the `X-Request-ID` and `X-Trace-ID` headers
    - Stores the IDs in the Mapped Diagnostic Context (MDC) for logging
    - Adds the IDs to the response headers
    - Makes the IDs available as request attributes

2. **BaseController** includes the request ID in all API responses:
    - Retrieves the ID from the request attribute
    - Adds it to the BaseResponse object using `withRequestId()`

3. **BaseResponse** carries the request ID:
    - The `requestId` field in BaseResponse preserves the ID
    - This ID appears in the API response JSON

4. **Logback Configuration** includes the request ID in all log entries:
    - The logback-spring.xml configuration includes `%X{requestId}` in the pattern
    - This extracts the request ID from the MDC and includes it in each log line

## ID Types

The system supports two types of IDs:

1. **Request ID**: Uniquely identifies a single HTTP request
    - Header: `X-Request-ID`
    - MDC Key: `requestId`
    - Prefix for generated IDs: `req-`

2. **Trace ID**: Used for distributed tracing across multiple services
    - Header: `X-Trace-ID`
    - MDC Key: `traceId`
    - Prefix for generated IDs: `gen-`

## Request-Log Correlation

With this setup:

1. A client makes an API request
2. The request gets unique request and trace IDs
3. All log entries related to this request include these IDs
4. The API response includes the request ID
5. The client can reference this ID when reporting issues

## Example Flow

1. Request comes in → Request ID `req-abc123` and Trace ID `gen-xyz789` are assigned
2. Log entry: `[req-abc123][gen-xyz789] Processing request`
3. Controller processes request → adds `req-abc123` to BaseResponse
4. Log entry: `[req-abc123][gen-xyz789] Request processing complete`
5. Response sent to client with `requestId: "req-abc123"` in the JSON body
6. Response includes `X-Request-ID: req-abc123` and `X-Trace-ID: gen-xyz789` headers

## Benefits

- Trace request processing throughout the system
- Track requests across multiple services
- Correlate logs with specific API calls
- Client applications can reference specific requests
- Support teams can quickly find relevant logs

## Custom IDs

Clients can supply their own request and trace IDs by including the corresponding headers in their requests.
This allows external systems to maintain their own tracking IDs across service boundaries. 