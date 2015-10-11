#!/usr/bin/env ruby

require 'openssl'
require 'optparse'
require 'json'


# Helper Methods

class NilClass
	def nil_or_empty?
    	true
    end
end

class String
	def nil_or_empty?
		empty?
	end
end

def base64_pack(str)
	[str].pack('m').strip
end

def base64_unpack(str64)
	str64.unpack('m')[0]
end


# We use the AES 256 bit cipher-block chaining symetric encryption
alg = "AES-256-CBC"

default_key = OpenSSL::Cipher::Cipher.new(alg).random_key
aes = OpenSSL::Cipher::Cipher.new(alg)


# Option Parsing

options = {
	:key => base64_pack(default_key)
}

option_parser = OptionParser.new do |opts|
	opts.on('-S SENDER', '--sender') do |sender|
		options[:sender] = sender
	end
	
	opts.on('-H HUBNAME', '--hubname') do |hubname|
		options[:hubname] = hubname
	end
	
	opts.on('-C CONNECTION', '--connection') do |connection|
		options[:connection_string] = connection
	end
	
	opts.on('-K KEY', '--key') do |key|
		options[:key] = key
	end
end
option_parser.parse!

we_have_all_values = not(
	options[:key].nil_or_empty? or
	options[:sender].nil_or_empty? or
	options[:hubname].nil_or_empty? or
	options[:connection_string].nil_or_empty?
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

iv = OpenSSL::Cipher::Cipher.new(alg).random_iv
key = base64_unpack(options[:key])
raise 'Key Error' if(key.nil? or key.size != 32)

aes.encrypt
aes.key = key
aes.iv = iv

encrypted_json = aes.update(settings_json)
encrypted_json << aes.final 

puts "Settings:\n#{JSON.pretty_generate(settings)}\n\n"
puts "Key:\n#{options[:key]}\n\n"
puts "Encrypted Settings:\n#{base64_pack(encrypted_json)}\n\n"
