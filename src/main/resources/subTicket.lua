local userId = KEYS[1]
local maxTicketNum = ARGV[1]
local timeStamp = tonumber(ARGV[2])

local endTime = tonumber(redis.call('HGET', 'globalDeal', 'endTime'))
if timeStamp >= endTime then
	return {'-11', 'It is already over.', userId}
end

local point = redis.call('HGET','user:'..userId,'point')
if point and point ~= nil then
	local userPoint = tonumber(point);
	local globalDealArr = redis.call('HMGET', 'globalDeal', 'price', 'sumTicket', 'startTime')
	local priceStr = globalDealArr[1];
	local price = tonumber(priceStr)
	local sumTicket = tonumber(globalDealArr[2])
	local startTime = tonumber(globalDealArr[3])
	if price <= 0 then
		return {'-10', 'System error.', userId}
	end
	if userPoint == 0 then
		return {'-101', 'Insufficient Balance.', userId}
	end
	
	local shareArr = {}
	local usedPoint = 0.0
	local currUsed = 0.0
	local dticketNum = 0.0
	local buyTicketNum = 0.0
	
	local i = 1
	while (userPoint - usedPoint >= price) do
	--[[
	while (userPoint - usedPoint >= 0.00000001) do
		if (userPoint - usedPoint) < price then
			dticketNum = (userPoint - usedPoint) / price
			currUsed = dticketNum * price
			buyTicketNum = buyTicketNum + dticketNum
			sumTicket = sumTicket + dticketNum
		else
		--]]
			currUsed = price
			buyTicketNum = buyTicketNum + 1
			sumTicket = sumTicket + 1
		--end
		usedPoint = usedPoint + currUsed
		if timeStamp - startTime >= 0 then
			price = price * 0.99999
		end
		
		shareArr[i] = (currUsed * 0.50) / sumTicket
		i = i+1
	end
	-- userPoint = userPoint - usedPoint
	userPoint = 0
	
	-- write data
	redis.call('HSET','user:'..userId,'point', userPoint)
	redis.call('HINCRBYFLOAT','user:'..userId,'ticketNum', buyTicketNum)
	redis.call('HINCRBYFLOAT','user:'..userId,'sumUsedPoint', usedPoint)
	redis.call('HMSET', 'globalDeal', 'price', price, 'sumTicket', sumTicket, 'lastTime', timeStamp)
	redis.call('HINCRBYFLOAT', 'globalDeal', 'sumUsedPoint', usedPoint)
	
	local userKeys = redis.call('KEYS', 'user:*')
	for i = 1, #userKeys do
		local isMe = false
		if 'user:'..userId == userKeys[i] then
			isMe = true
		end
		
		local tknum = tonumber(redis.call('HGET', userKeys[i], 'ticketNum'))
		if isMe or (tknum and tknum > 0) then
			local sumDv = 0
			for j = 1, #shareArr do
				if isMe then
					local tknumTmp = tknum - buyTicketNum + j
					if tknumTmp > tknum then
						tknumTmp = tknum
					end
					-- redis.call('ZADD', 'tmppp', j, tknumTmp)
					sumDv = sumDv + tknumTmp * shareArr[j]
				else
					sumDv = sumDv + tknum * shareArr[j]
				end
			end
			redis.call('HINCRBYFLOAT', userKeys[i], 'innerPoint', sumDv)
		end
	end
	
	-- end time
	local timeCtrls = redis.call('HMGET', 'globalDeal', 'countDownType', 'countDownMax', 'countDownIncr', 'endTime')
	local countDownMax = tonumber(timeCtrls[2])
	local countDownIncr = tonumber(timeCtrls[3])
	local endTime = tonumber(timeCtrls[4])
	if timeCtrls[1] == '1' then
		redis.call('HSET', 'globalDeal', 'endTime', tostring(timeStamp + countDownMax))
	elseif timeCtrls[1] == '2' then
		local tmpTime = endTime + countDownIncr - timeStamp
		if tmpTime > countDownMax then
			tmpTime = countDownMax
		end
		redis.call('HSET', 'globalDeal', 'endTime', tostring(timeStamp + tmpTime))
	end
	
	-- log
	local logId = redis.call('INCRBY', 'dealLog:newAddNum', 1)
	local jsonv = '{"uid":' .. userId .. ', "ticket":' .. buyTicketNum .. ', "price":' .. priceStr .. ', "usedPoint":' .. usedPoint
	jsonv = jsonv .. ', "point":' .. point .. ', "time":' .. timeStamp .. '}'
	redis.call('RPUSH', 'dealLog:logList', jsonv)
	
	return {tostring(buyTicketNum), priceStr, tostring(usedPoint)}
else
	return {'-100', 'No user info.', userId}
end
