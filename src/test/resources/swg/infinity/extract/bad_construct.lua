-- Lua construct the reader does not handle: a function literal.
-- The parser is fail-closed; encountering 'function' must raise.
local bad = function(x) return x + 1 end
