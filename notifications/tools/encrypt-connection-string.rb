#!/usr/bin/env ruby

require 'openssl'
require 'optparse'
require 'json'


# Helper Methods

def base64_pack(str)
	[str].pack('m').strip
end

def base64_unpack(str64)
	str64.unpack('m')[0]
end


# We use the AES 256 bit cipher-block chaining symetric encryption
cipher = OpenSSL::Cipher::Cipher.new("AES-256-CBC")


# Option Parsing

options = {
	:iv => base64_pack(cipher.random_iv),
	:key => base64_pack(cipher.random_key),
	:sender => '',
	:hubname => '',
	:connection_string => ''
}

option_parser = OptionParser.new do |opts|
	opts.on('-I IV', '--iv') do |iv|
		options[:iv] = iv
	end
	
	opts.on('-K KEY', '--key') do |key|
		options[:key] = key
	end
	
	opts.on('-S SENDER', '--sender') do |sender|
		options[:sender] = sender
	end
	
	opts.on('-H HUBNAME', '--hubname') do |hubname|
		options[:hubname] = hubname
	end
	
	opts.on('-C CONNECTION', '--connection') do |connection|
		options[:connection_string] = connection
	end
end
option_parser.parse!

we_have_all_values = not(
	options[:iv].empty? or
	options[:key].empty? or
	options[:sender].empty? or
	options[:hubname].empty? or
	options[:connection_string].empty?
)

unless we_have_all_values
	puts option_parser
	exit
end


# Generate Settings JSON

settings = {
	:sender => options[:sender],
	:hubname => options[:hubname],
	:connection_string => options[:connection_string]
}
settings_json = JSON.generate(settings)


# Encrypt settings JSON

key = base64_unpack(options[:key])
iv = base64_unpack(options[:iv])
raise 'Key Error' if (key.nil? or key.size != 32)

cipher.encrypt
cipher.key = key
cipher.iv = iv

encrypted_json =  cipher.update(settings_json)
encrypted_json << cipher.final

puts "Settings:\n#{JSON.pretty_generate(settings)}\n\n"
puts "Key:\n#{options[:key]}\n\n"
puts "IV:\n#{options[:iv]}\n\n"
puts "Encrypted Settings:\n#{base64_pack(encrypted_json)}\n\n"
